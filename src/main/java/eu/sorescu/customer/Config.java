package eu.sorescu.customer;

import eu.sorescu.customer.hid.FilePicker;

import javax.swing.*;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Config {
    static final File INPUT_FILES_FOLDER=new File(new File("."),"input-files");
    static final File DEFAULT_FILE=new File(INPUT_FILES_FOLDER,"stock_price_data_files.zip");
    private Config(){
        // no init required
    }
    private static final Config INSTANCE=new Config();
    public static synchronized Config getInstance(){
        return INSTANCE;
    }
    private Map<String,Object> _CACHE=new ConcurrentHashMap<>();
    public File getInputFile(){
        return (File)_CACHE.computeIfAbsent("inputFile",key-> FilePicker.pickFile(DEFAULT_FILE)
                .orElseThrow(() -> {
                    JOptionPane.showMessageDialog(null, "No file was selected.");
                    System.exit(-1);
                    return new RuntimeException("Not reachable");
                }));
    }

    public int getFilesLimit() {
        return (Integer)_CACHE.computeIfAbsent("filesLimit",key->{
           for(;;){
               String value=JOptionPane.showInputDialog("Number of files (only digits): ");
               if(value.matches("^\\d{1,3}$"))return Integer.parseInt(value);
               if(value.isEmpty())System.exit(-1);
               JOptionPane.showConfirmDialog(null,"Not a number. Leave blank to quit.");
           }
        });
    }
    public File getOutputFolder(){
        return getInputFile().getParentFile();
    }
}
