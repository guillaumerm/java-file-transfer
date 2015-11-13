package travail_pratique_4;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

/**
 * Classe controller pour une application qui permet de transfèrer un fichier
 * d'un ordianteur à un autre.
 *
 * @author Guillaume Rochefort-Mathieu & Terry Turcotte
 */
public class FXMLDocumentController implements Initializable, Observer {

    private final static String pathDownload = "C:/Users/" + System.getProperty("user.name") + "/Downloads/";
    private final static char END_OF_TRANSMISSION = ((char) 37);
    private final static char DATA_LINK_ESCAPE = ((char) 16);
    private final static char START_OF_HEADING = ((char) 1);

    private Sender client;
    private Receiver server;
    private File fichierSortant;
    private File fichierEntrant = null;
    private OutputStream out = null;
    private InputStream fis = null;
    private int content = -1;
    private int taille = 1024;
    private final ObservableList<String> trameHistorique = FXCollections.observableArrayList();
    private EtatClient etatClient = EtatClient.READY;
    private int numErreurTrame = 0;

    /**
     * Les états du client qui s'occupe de l'envoie d'un fichier
     */
    private enum EtatClient {

        READY, TRANSMITTING, FINISHING
    }

    @FXML
    private TextField fileChooser_textfield;

    @FXML
    private TextField ipAddress_textfield;

    @FXML
    private ListView trame_listview;

    @FXML
    private TextField size_textfield;

    @FXML
    private TextField trameErreur_textfield;

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
     * Méthode appelé lorsqu'on click sur le bouton send.
     *
     * @param event la node graphique qui appel cette indication
     */
    @FXML
    private void onSend(ActionEvent event) {
        trameHistorique.clear();
        etatClient = EtatClient.READY;

        try {
            InetAddress addressDestination = InetAddress.getByName(ipAddress_textfield.getText());
            client = new Sender(addressDestination.getAddress());
            client.addObserver(this);
        } catch (UnknownHostException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            taille = Integer.parseInt(size_textfield.getText());
        } catch (NumberFormatException e) {
            taille = 1024;
        }

        try {
            numErreurTrame = Integer.parseInt(trameErreur_textfield.getText());
        } catch (NumberFormatException e) {
            numErreurTrame = 0;
        }

        client.start(numErreurTrame);
    }

    /**
     * Méthode appelé lorsque l'application qui pour fermer les sockets
     * transmetteur et recepteur.
     */
    public void stop() {
        if (client != null) {
            client.stop();
        }

        server.stop();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        trame_listview.setItems(trameHistorique);
        server = new Receiver();
        server.addObserver(this);
        server.start();
    }

    /**
     * Ajoute l'information à la vue pour un historique des échanges des SEQ et
     * ACK
     *
     * @param type type de la trame
     * @param num numero de la trame
     */
    private void ajouterTraceTrame(byte type, byte num, String description) {
        String typeString = (type == Trame.TRAME_ACK) ? "ACK" : "SEQ";
        String numString = (num == Trame.TRAME_NUM0) ? "0" : "1";

        Platform.runLater(() -> {
            trameHistorique.add(typeString + " " + numString + " " + description);
        });
    }

    /**
     * Démonte la trame et interpret les données envoyé.
     *
     * @param trame
     */
    private void demonterTrame(Trame trame) {
        if (trame.type == Trame.TRAME_ENVOIE) {
            if (trame.message[0] == START_OF_HEADING && trame.message[1] == DATA_LINK_ESCAPE) {
                //Cette trame contient le nom du fichier qui sera transmis
                String filename = "";

                for (int i = 2; i < trame.message.length; i++) {
                    try {
                        filename += new String(new byte[]{trame.message[i]}, "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                try {
                    out = new FileOutputStream(fichierEntrant = new File(pathDownload + filename));
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                //Cette trame contient des données du fichier qui est transmis
                if (out != null) {
                    writeToFile(trame.message);
                }
            }
        }
    }

    /**
     *
     * @param message
     */
    public void writeToFile(byte[] message) {
        try {
            if (message != null) {
                if (message[0] == END_OF_TRANSMISSION && message[1] == DATA_LINK_ESCAPE) {
                    //Indicateur de fin de transmission
                    out.flush();
                    out.close();
                    out = null;
                } else {
                    out.write(message);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Obtient le contenu nécessaire pour le transfère tout dépendant de l'état
     * du client.
     *
     * @return buffer pour l'envoie
     */
    private byte[] obtenirContenuPourTransfere() {

        byte[] data = new byte[taille];

        if (etatClient == EtatClient.READY) {
            // Le client transmetera en premier lieu le nom du fichier qui sera transmis
            data = new byte[fichierSortant.getName().getBytes().length + 2];

            data[0] = START_OF_HEADING;
            data[1] = DATA_LINK_ESCAPE;

            System.arraycopy(fichierSortant.getName().getBytes(), 0, data, 2, data.length - 2);

            try {
                fis = new FileInputStream(fichierSortant);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }

            etatClient = EtatClient.TRANSMITTING;

        } else if (etatClient == EtatClient.TRANSMITTING) {
            // Tant qu'il restera des octets du fichier à transférer le client reste dans l'état transmettre.
            try {

                // Si le nombre d'octets restant est inférieur au taille du buffer saisie
                if (fis.available() < taille) {
                    data = new byte[fis.available()];
                }

                int bytesRead = 0;

                do {
                    content = fis.read();

                    if (content != -1) {
                        data[bytesRead] = (byte) content;
                        bytesRead++;
                    } else {
                        fis.close();
                        etatClient = EtatClient.FINISHING;
                    }
                } while ((content != -1) && bytesRead < taille);
            } catch (IOException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (etatClient == EtatClient.FINISHING) {
            //Si le client à terminé le transférer du fichier l'indiquer au recepteur
            data = new byte[]{END_OF_TRANSMISSION, DATA_LINK_ESCAPE};
        }

        return data;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof Receiver && arg instanceof Trame) {
            Trame trame = (Trame) arg;
            demonterTrame(trame);
            
            ajouterTraceTrame(trame.type, trame.numero, "TRANSMISSION ENTRANT: " + fichierEntrant.getName());
        } else if (o instanceof Sender) {
            // Sender est la source de l'indication
            if (arg instanceof Trame) {
                // Si l'objet transmis par le Sender est une trame
                Trame trame = (Trame) arg;
                ajouterTraceTrame(trame.type, trame.numero,"TRANSMISSION SORTANT: " + fichierSortant.getName());
            } else {
                // Recois l'indication de remplir le buffer du sender
                ((Sender) o).setBuffer(obtenirContenuPourTransfere());
            }
        }
    }
}
