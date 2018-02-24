/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greetingsserver;

/**
 *
 * @author Korisnik
 */
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import sun.misc.BASE64Decoder;

public class GreetingsServer /*extends Thread*/ {

    private static ServerSocket serverSocket;
    private static int port = 6588;
    static Socket server;
    
    static String msg_received;
    static String rezultati_str;

    public GreetingsServer(int port) throws IOException, SQLException, ClassNotFoundException, Exception {
        serverSocket = new ServerSocket(port);
    }

//tmp

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException, Exception {
   
        serverSocket = new ServerSocket(port);
        server = serverSocket.accept();
        
        DataInputStream DIS = new DataInputStream(server.getInputStream());
        DataOutputStream DOS = new DataOutputStream(server.getOutputStream());

        msg_received = DIS.readUTF();
        System.out.println(msg_received);//radi

        BufferedImage image = null;
        byte[] imageByte;

        BASE64Decoder decoder = new BASE64Decoder();
        imageByte = decoder.decodeBuffer(msg_received);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
        image = ImageIO.read(bis);
        bis.close();
        ImageIO.write(image, "JPG", new File("C:\\Users\\Korisnik\\Documents\\NetBeansProjects\\GreetingsServer\\slika.jpg"));

        
        call_exe();
        //rezultati_str = read_result(new FileReader("C:\\Users\\Korisnik\\Documents\\NetBeansProjects\\GreetingsServer\\file.txt"));
        rezultati_str = read_result(new FileReader("C:\\Users\\Korisnik\\Desktop\\file.txt"));
        DOS.writeUTF(rezultati_str);

    }

//    void save(byte[] bytes) {
//        //File file12= new File("D:\\protobuf\\protobuf_java_prb1\\read_bytes_from_file\\slika.jpg");
//        File file12 = new File("D:\\protobuf\\protobuf_java_prb1\\read_bytes_from_file\\slika3.txt");
//        OutputStream outputStream = null;
//        try {
//            outputStream = new FileOutputStream(file12);
//            outputStream.write(bytes);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (outputStream != null) {
//                    outputStream.close();
//                }
//            } catch (Exception e) {
//            }
//        }
//    }

    static String read_result(FileReader f) {

        String rez;
        StringBuilder sb = new StringBuilder();

        try {

            BufferedReader br = new BufferedReader(f);
            try {
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                }
            } finally {
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        rez = sb.toString();
        return rez;
    }

    static void call_exe() {

        String filePath = "darknet_no_gpu.exe";
        String param1 = "detector";
        String param2 = "test";
        String param3 = "data/voc.data";
        String param4 = "yolo-voc.cfg";
        String param5 = "yolo-voc.weights";
        String param6 = "-i";
        String param7 = "0";
        String param8 = "slika.jpg";
        if (new File(filePath).exists()) {
            try {
                ProcessBuilder pb = new ProcessBuilder(filePath, param1, param2, param3, param4, param5, param6, param7, param8);
                pb.redirectError();
                Process p = pb.start();

                InputStream is = p.getInputStream();
                int value = -1;

                while ((value = is.read()) != -1) {
                    System.out.print((char) value);
                }

                int exitCode = p.waitFor();

                System.out.println(filePath + " exited with " + exitCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println(filePath + " does not exist");
        }
    }

}
