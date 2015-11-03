/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package travail_pratique_4;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

/**
 *
 * @author Guillaume
 */
public class FXMLDocumentController implements Initializable {

    private UDPClient client;
    private File fichier;
    private InetAddress addressDestination;

    @FXML
    private Button send_button;

    @FXML
    private TextField fileChooser_textfield;

    @FXML
    private TextField ipAddress_textfield;

    /**
     * Méthode qui ouvre un file chooser et ensuite change le path dans la vue
     * pour le fichier sélectionné.
     */
    @FXML
    private void onOpenFileChooser(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Ouvrir fichier text à envoyer");
        File fichierTemp = chooser.showOpenDialog(((Button) event.getSource()).getScene().getWindow());
        
        if(fichierTemp !=null){
            fichier = fichierTemp;
            fileChooser_textfield.setText(fichier.getAbsolutePath());
        }
    }

    @FXML
    private void onSend(ActionEvent event) {
        try {
            addressDestination = InetAddress.getByName(ipAddress_textfield.getText());
        } catch (UnknownHostException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }

        client = new UDPClient(addressDestination.getAddress());
        
        byte[] message = readTextFile();
        
        new Thread(()->{client.sendData(message);}).start();  
    }

    private byte[] readTextFile() {
        InputStream fis = null;
        
        try {
            fis = new FileInputStream(fichier);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        BufferedInputStream bis = new BufferedInputStream(fis);
        
        byte[] data = new byte[(int) fichier.length()];
        int content;
        int bytesRead = 0;
        
        try {
            while((content = fis.read()) != -1){
                data[bytesRead] = (byte) content;
                bytesRead++;
            }
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return data;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }
}
