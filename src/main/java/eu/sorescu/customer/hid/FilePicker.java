package eu.sorescu.customer.hid;

import javax.swing.*;
import java.io.File;
import java.util.Optional;

public class FilePicker {
    public static Optional<File> pickFile(File defaultFile){
        if(defaultFile!=null && defaultFile.exists())return Optional.of(defaultFile);
        JFileChooser fc = new JFileChooser();
        int result=fc.showDialog(null,"Select");
        if((result==JFileChooser.APPROVE_OPTION)&&(fc.getSelectedFile().exists()))
            return Optional.of(fc.getSelectedFile());
        else {
            return Optional.empty();
        }
    }
}
