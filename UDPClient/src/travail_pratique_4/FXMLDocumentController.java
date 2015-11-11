/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package travail_pratique_4;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
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

    private Sender client;
    private File fichierSortant;
    private InetAddress addressDestination;
    private String pathDownload = "C:/Users/" + System.getProperty("user.name") + "/Downloads/";
    private File fichierEntrant = null;
    private final static char END_OF_TRANSMISSION = ((char) 37);
    private final static char DATA_LINK_ESCAPE = ((char) 16);
    private final static char START_OF_HEADING = ((char) 1);
    private FileOutputStream out = null;
    private InputStream fis = null;
    private boolean isClosed = true;
    private int content = -1;
    private int taille = 1024;

    @FXML
    private TextField fileChooser_textfield;

    @FXML
    private TextField ipAddress_textfield;

    @FXML
    private TextArea trame_textarea;

    @FXML
    private TextField size_textfield;

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
            fichierSortant = fichierTemp;
            fileChooser_textfield.setText(fichierSortant.getAbsolutePath());
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

        taille = Integer.parseInt(size_textfield.getText());

        client = new Sender(addressDestination.getAddress());
        client.addObserver(this);

        new Thread(() -> {
            client.start();
        }).start();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Receiver server = new Receiver();
        server.addObserver(this);
        server.start();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof Receiver) {
            byte[] message = (byte[]) arg;

            // Recois l'indication d'ecrire le message
            if (message[0] == START_OF_HEADING) {
                String filename = "";
                for (int i = 1; i < message.length; i++) {
                    filename += new String(new byte[]{message[i]});
                }
                createFile(filename);
            } else {
                writeToFile(message);
            }
        } else if (o instanceof Sender) {
            // Recois l'indication d'obtenir le message
            ((Sender) o).setBuffer(readFromFile());
        }
    }

    public void writeToFile(byte[] message) {
        try {
            if (message != null) {
                if (message[0] == END_OF_TRANSMISSION && message[1] == DATA_LINK_ESCAPE) {
                    out.flush();
                    out.close();
                } else {
                    out.write(message);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @return
     */
    private byte[] readFromFile() {

        byte[] data = new byte[taille];

        int bytesRead = 0;

        if (fis == null) {
            int longueurFilenameBytes = fichierSortant.getName().getBytes().length + 1;
            data = new byte[longueurFilenameBytes];
            data[0] = START_OF_HEADING;

            System.arraycopy(fichierSortant.getName().getBytes(), 0, data, 1, data.length - 1);

            try {
                fis = new FileInputStream(fichierSortant);
                //bis = new BufferedInputStream(fis);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }

            isClosed = false;
        } else {
            try {

                if (!isClosed) {

                    // Si le nombre de octets restant est inférieur au taille du buffer saisie
                    if (fis.available() < taille) {
                        data = new byte[fis.available()];
                    }

                    do {
                        content = fis.read();
                        if (content != -1) {
                            data[bytesRead] = (byte) content;
                            bytesRead++;
                        } else {
                            fis.close();
                            isClosed = true;
                        }
                    } while ((content != -1) && bytesRead < taille);
                } else if (isClosed) {
                    data[0] = END_OF_TRANSMISSION;
                    data[1] = DATA_LINK_ESCAPE;
                }
            } catch (IOException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return data;
    }

    private void createFile(String filename) {
        try {
            out = new FileOutputStream(fichierEntrant = new File(pathDownload + filename));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
