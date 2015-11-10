/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package travail_pratique_4;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

/**
 *
 * @author Guillaume
 */
public class FXMLDocumentController implements Initializable, Observer {

    private NetworkModule networkModule;
    private File fichier;
    private InetAddress addressDestination;
    private File file = new File("C:/Users/" + System.getProperty("user.name") + "/Downloads/text.txt");
    private final static char END_OF_TRANSMISSION = ((char) 37);
    private FileOutputStream out = null;
    private BufferedOutputStream bos = null;

    @FXML
    private TextField fileChooser_textfield;

    @FXML
    private TextField ipAddress_textfield;

    @FXML
    private TextArea trame_textarea;

    /**
     * Méthode qui ouvre un file chooser et ensuite change le path dans la vue
     * pour le fichier sélectionné.
     */
    @FXML
    private void onOpenFileChooser(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Ouvrir fichier text à envoyer");
        File fichierTemp = chooser.showOpenDialog(((Button) event.getSource()).getScene().getWindow());

        if (fichierTemp != null) {
            fichier = fichierTemp;
            fileChooser_textfield.setText(fichier.getAbsolutePath());
        }
    }

    /**
     *
     * @param event
     */
    @FXML
    private void onSend(ActionEvent event) {
        try {
            addressDestination = InetAddress.getByName(ipAddress_textfield.getText());
        } catch (UnknownHostException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }

        networkModule.setAddress(addressDestination.getAddress());


        networkModule.startClient(fichier);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        networkModule = new NetworkModule(9786);
        networkModule.addObserver(this);
        networkModule.startServer();

        try {
            out = new FileOutputStream(file);
            bos = new BufferedOutputStream(out);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NetworkModule.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof NetworkModule) {
            trame_textarea.setPrefRowCount(trame_textarea.getPrefRowCount());
            try {
                trame_textarea.setText(new String(((byte[]) arg), "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
            writeToFile((byte[]) arg);
        }
    }

    public void writeToFile(byte[] message) {
        try {
            if (message != null) {
                if (message[0] == END_OF_TRANSMISSION) {
                    out.flush();
                    out.close();
                } else {
                    out.write(message);
                }
            } else {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Logger.getLogger(NetworkModule.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(NetworkModule.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
