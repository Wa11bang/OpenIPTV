import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class VLCPlayer {

    private static JFileChooser filechooser = new JFileChooser();
//This is the path for libvlc.dll

    public static void main(String[] args) {
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:\\Program Files\\VideoLAN\\VLC");
        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
        SwingUtilities.invokeLater(() -> {
            VLCPlayer vlcPlayer = new VLCPlayer();
        });

    }

    private VLCPlayer() {

//MAXIMIZE TO SCREEN
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

        JFrame frame = new JFrame();
        MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory();

        Canvas c = new Canvas();
        c.setBackground(Color.black);
        JPanel p = new JPanel();
        c.setBounds(100, 500, 1050, 500);
        p.setLayout(new BorderLayout());
        p.add(c, BorderLayout.CENTER);
        p.setBounds(100, 50, 1050, 600);
        frame.add(p, BorderLayout.NORTH);
        JPanel p1 = new JPanel();

        p1.setBounds(100, 900, 105, 200);
        frame.add(p1, BorderLayout.SOUTH);

        JButton playbutton = new JButton("play");

        //playbutton.setIcon(new ImageIcon("C:/Users/biznis/Desktop/Newspaper/sangbadpratidin/d/download.png"));
        playbutton.setBounds(50, 50, 150, 100);
        // playbutton.addActionListener((ActionListener) this);
        p1.add(playbutton);

        JButton pausebutton = new JButton("pause");

        //  pausebutton.setIcon(new ImageIcon("pics/pausebutton.png"));
        pausebutton.setBounds(80, 50, 150, 100);

        p1.add(pausebutton);
        File ourfile;

        filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        filechooser.showSaveDialog(null);
        ourfile = filechooser.getSelectedFile();
        String mediaPath = ourfile.getAbsolutePath();

        EmbeddedMediaPlayer mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        mediaPlayer.videoSurface().set(mediaPlayerFactory.videoSurfaces().newVideoSurface(c));
        frame.setLocation(100, 100);
        frame.setSize(1050, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        mediaPlayer.media().play(mediaPath);
        pausebutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                mediaPlayer.controls().pause();
                // or mediaPlayer.pause() depending on what works.
            }
        });
        playbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                mediaPlayer.controls().play();
            }
        });
    }
}