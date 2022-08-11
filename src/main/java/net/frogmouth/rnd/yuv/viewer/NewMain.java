package net.frogmouth.rnd.yuv.viewer;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

public class NewMain extends JFrame {

    public NewMain() {

        initUI();
    }
    
    private void initUI() {
        
        try {
            add(new MoviePanel());
            
            setResizable(false);
            pack();
            
            setTitle("YUV");
            setLocationRelativeTo(null);        
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } catch (IOException ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        
        EventQueue.invokeLater(() -> {
            JFrame ex = new NewMain();
            ex.setVisible(true);
        });
    }
}