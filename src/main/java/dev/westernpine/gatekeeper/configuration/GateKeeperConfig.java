package dev.westernpine.gatekeeper.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import proj.api.marble.builders.config.Config;

public class GateKeeperConfig {
	
	private List<String> toAdd = new ArrayList<>(ConfigValue.getStrings());
    
    private HashMap<ConfigValue, String> variables = new HashMap<>();

    public GateKeeperConfig(String[] launchArgs, String configPath, String fileName) {
        String string = " " + String.join(" ", launchArgs);

        if(string != null && string != "" && string.contains(" -")) { //makes sure there are launch arguements
            launchArgs = string.split(" -"); //splits the different arguements
            
            for (String arg : launchArgs) {
                
                if(arg.contains(" ") && arg.split(" ").length > 1) { //makes sure there is an id and value
                    String[] array = arg.split(" ");
                    
                    ConfigValue cv = ConfigValue.valueOf(array[0]); //the id/tag/argument such as id in "-id 0"
                    
                    if (cv != null) 
                        variables.put(cv, (array.length > 1 ? String.join(" ", Arrays.copyOfRange(array, 1, array.length)) : array[1])); //removes/ignores id/tag/argument
                }
            }
        }
        
        filterAdditions();
        
        for(String key : System.getenv().keySet()) {
        	ConfigValue cv = ConfigValue.valueOf(key);
            if(cv != null) 
                variables.put(cv, System.getenv(key));
        }
        
        filterAdditions();
        
        Config config = Config.builder().file(new File(configPath, fileName)).build();
        for(String var : toAdd) {
            variables.put(ConfigValue.valueOf(var), (String)config.get(var, "?"));
        }
        config.save();
        config = null;
        toAdd = null;
    }
    
    public String getValue(ConfigValue value) {
    	return variables.get(value);
    }
    
    private void filterAdditions() {
        List<Object> toRemove = new ArrayList<>();
        for(String testing : toAdd) {
            for(ConfigValue var : variables.keySet()) {
                if(var.toString().equalsIgnoreCase(testing)) {
                    toRemove.add(testing);
                }
            }
        }
        for(Object remove : toRemove) {
            toAdd.remove(remove);
        }
    }
    
}
