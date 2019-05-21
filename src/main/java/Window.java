import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;


            //ну тут нечего объяснять, это окно появляется когда все песни нашли свои парочки
//тут мы мануально решаем судьбу тех, кто вроде как нашел, но не совсем они совместимы
public class Window extends JFrame {
    int count = 0;
    public static void main(String[] args){


    }

    public static void runWindow(){
        //со свингом нельзя работать из потоков, только из потока ивент диспетч тред поэтому пишем следующую конструкцию
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Window();
            }
        });
    }


    private Window(){

        JFrame f=new JFrame("Песни, у которых названия схожи на 60%+");
        JButton right  = new JButton("ДА");
        JButton wrong  = new JButton("НЕТ");

        JTextField songPath  = new JTextField();
        JTextField lyricsPath  = new JTextField();

        JLabel songLabel = new JLabel("ПУТЬ К ПЕСНЕ:");
        JLabel lyricsLabel = new JLabel("ПУТЬ К ТЕКСТУ:");

        JLabel countLbl = new JLabel("Файлы: ../..");

        songLabel.setBounds(50,10,150, 40);
        lyricsLabel.setBounds(50,60,150, 40);

        songPath.setBounds(25,50,550, 20);
        lyricsPath.setBounds(25,100,550, 20);

        right.setBounds(100,200,140, 40);
        wrong.setBounds(300,200,140, 40);

        countLbl.setBounds(200,250,150, 40);

        songPath.setEditable(false);
        lyricsPath.setEditable(false);


        f.add(right);
        f.add(wrong);
        f.add(songPath);
        f.add(lyricsPath);
        f.add(songLabel);
        f.add(lyricsLabel);
        f.add(countLbl);
        f.setSize(600,300);
        f.setLayout(null);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        songPath.setText(Main.pendingList.get(count).getSongsPath());
        lyricsPath.setText(Main.pendingList.get(count).getLyricsPath());
        right.addActionListener(new ActionListener() {

            boolean listEnded = false;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (!listEnded) {
                    System.out.println("Добавили: "+ Main.pendingList.get(count).getLyricsPath() + "\n" + Main.pendingList.get(count).getSongsPath());

                    Main.connectLyrics(lyricsPath.getText(), songPath.getText());

                    songPath.setText(Main.pendingList.get(count).getSongsPath());
                    lyricsPath.setText(Main.pendingList.get(count).getLyricsPath());
                    countLbl.setText("Файлы: "+String.valueOf(count+1)+"/"+Main.pendingList.size());
                }


                if (count<Main.pendingList.size()-1) {
                    count++;
                    songPath.setText(Main.pendingList.get(count).getSongsPath());
                    lyricsPath.setText(Main.pendingList.get(count).getLyricsPath());
                } else {
                    listEnded=true;
                    songPath.setText("ПЕСНИ ЗАКОНЧИЛИСЬ");
                    lyricsPath.setText("ПЕСНИ ЗАКОНЧИЛИСЬ");
                }
            }
        });
        wrong.addActionListener(new ActionListener() {

            boolean listEnded = false;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (!listEnded) {
                    System.out.println("НЕ добавили: " + Main.pendingList.get(count).getLyricsPath() + "\n" + Main.pendingList.get(count).getSongsPath());
                    songPath.setText(Main.pendingList.get(count).getSongsPath());
                    lyricsPath.setText(Main.pendingList.get(count).getLyricsPath());
                }


                if (count<Main.pendingList.size()-1) {
                    count++;
                    songPath.setText(Main.pendingList.get(count).getSongsPath());
                    lyricsPath.setText(Main.pendingList.get(count).getLyricsPath());
                } else {
                    listEnded=true;
                    songPath.setText("ПЕСНИ ЗАКОНЧИЛИСЬ");
                    lyricsPath.setText("ПЕСНИ ЗАКОНЧИЛИСЬ");
                }
            }
        });
    }


}


