import java.io.*;
import java.util.*;

import com.mpatric.mp3agic.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class Main {
        //тексты
    static private HashMap<ConnectorName, String>  lyricsMap = new HashMap<>();
        //песни
    static private HashMap<ConnectorName, String>  songsMap = new HashMap<>();
        //список, требующих дальнейшего рассмотрения
    static  ArrayList<TwoStrings> pendingList = new ArrayList<>();
    static Main fw = new Main();

    //static ArrayList<TwoStrings> pendingList = new ArrayList<>();
    static String buf = " lyrics.txt";
        //путь к папке с песнями
    static final String SONGS_PATH = "E:\\iTunes Media\\Music";
        //путь к папке с текстами
    static final String LYRICS_PATH = "E:\\Download\\LYRICS";

    public static void main(String[] args) throws InvalidDataException, IOException, UnsupportedTagException {
        //заполням тексты
        fillLyricsMap();
        //заполням песни
        fillSongsMap();
        //проверяем схожесть
        checkCompatibility();
        //если остались элементы, у которых схожесть >60% но <80, или сколько я там процентов поставил, тогда запускаем ручное одобрение
        if (pendingList.size()!=0){
            Window.runWindow();
        }
    }
        //проход ой мне так лень все эти комментарии писать, кто их вообще будет читать, ладно, основное подпишу, чтоб самому не потеряться потом
        //проходим, значит, наши песни/тексты, и заполняем ими хэшмапы
    public void walk( String path, HashMap<ConnectorName, String> hashMap, boolean isSong) throws InvalidDataException, IOException, UnsupportedTagException { //isSong -- ищем ли мы песни, для них будут дополнительные события

        File root = new File(path);
        File[] list = root.listFiles();

        String tempArtist = null;
        String tempAlbum = null;
        String tempSong = null;
        String tempPath = null;

        if (list == null) return;
        //проходим по файлам
        for ( File f : list ) {
            if ( f.isDirectory() ) {
                walk( f.getAbsolutePath(), hashMap, isSong);
                System.out.println( "Dir:" + f.getAbsoluteFile() + " PARENT: " + f.getAbsoluteFile().getParentFile().getName());
            }
            else {
                System.out.println( "File:" + f.getAbsoluteFile() );
                    //если метод вызван для НЕпесни, то есть для текста
                if(!isSong) {

                    tempPath = String.valueOf(f.getAbsoluteFile());
                    tempSong = f.getAbsoluteFile().getName();
                    tempAlbum = f.getAbsoluteFile().getParentFile().getName();
                    tempArtist = f.getAbsoluteFile().getParentFile().getParentFile().getName();
                    System.err.println("ФАЙЛЙЛЫЫ!:: " + tempSong + "    " + tempAlbum + "    " + tempArtist + " PATH: " + tempPath);

                    // удаляем окончание файла " lyrics.txt" и имя артиста + дефис
                    tempSong = tempSong.replace(buf, "");
                    tempSong = tempSong.replace(tempArtist + " - ", "");

                    // удаляем лишние символы
                    tempSong = tempSong.replaceAll("[_\'\".&(),!\\s]", "");
                    tempAlbum = tempAlbum.replaceAll("[_\'\".&(),!\\s]", "");
                    tempArtist = tempArtist.replaceAll("[_\'\".&(),!\\s]", "");

                    // переводим в нижний регистр
                    tempSong = tempSong.toLowerCase();
                    tempAlbum = tempAlbum.toLowerCase();
                    tempArtist = tempArtist.toLowerCase();
                    // если в названии есть feat то удаляем к чертям и фит, и все, что после него
                    tempSong = StringUtils.substringBefore(tempSong, "feat");
                    //добавляем
                    hashMap.put(new ConnectorName(tempArtist, tempAlbum, tempSong), tempPath);
                } else {
                    // если всё-таки песня
                    //получаем ид3 теги нашей песни

                    try {
                        tempPath = String.valueOf(f.getAbsoluteFile());
                        Mp3File mp3file = new Mp3File(f.getAbsoluteFile());
						obsoleteStage(mp3file);
						  
                        if (mp3file.getId3v2Tag().getPaymentUrl()!=null && mp3file.getId3v2Tag().getPaymentUrl().equals("lyricsReady")) {
                            writeFile(String.valueOf(f.getAbsoluteFile()),"", "есть тег lyricsReady", "lyricsReadyERRORS");
                            return;
                        }

                        ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                        //получаем имена
                        String song = id3v2Tag.getTitle();
                        String artist = id3v2Tag.getArtist();
                        String album = id3v2Tag.getAlbum();
                        System.out.println("gtcyz^ " + song);
                        //всё то же самое
                        song = song.toLowerCase();
                        album = album.toLowerCase();
                        artist = artist.toLowerCase();

                        System.out.println("\u001B[32m TEST: " + song);
                        song = StringUtils.substringBefore(song, "feat");
                        System.out.println("TEST2: " + song);
                        System.out.println("\u001B[0m");

                        song = song.replaceAll("[_\'\".&(),!\\s]", "");
                        artist = artist.replaceAll("[_\'\".&(),!\\s]", "");
                        album = album.replaceAll("[_\'\".&(),!\\s]", "");

                        hashMap.put(new ConnectorName(artist, album, song), tempPath);
                        //иногда программа думает что осилит формат, например m4a, и выдает NullPointerException, а иногда просто InvalidDataException, если сразу понимает, что не потянет
                        //можно было бы сделать нормальную проверку, но я ведь не в продакшн делаю, а для себя. и так сойдет
                    } catch (InvalidDataException | NullPointerException | IllegalArgumentException  e){
                        System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                                writeFile(tempPath, String.valueOf(e), "", "nompegsfound");
                    }

                }


            }
        }
    }

        //собвственно, вызываем предыдущий метод
    private static void fillLyricsMap() throws InvalidDataException, IOException, UnsupportedTagException {

        fw.walk(LYRICS_PATH, lyricsMap, false);

        lyricsMap.forEach((left, right) ->{
            System.err.println("Название: " + left.getSong());
            System.err.println("Исполнитель: " + left.getArtist());
            System.err.println("Альбом: " + left.getAlbum());
            System.err.println("Путь: " + right);
            System.out.println("\n\n");
        } );


    }
    //собвственно, вызываем предыдущий метод
    private static void fillSongsMap() throws InvalidDataException, IOException, UnsupportedTagException {

        fw.walk(SONGS_PATH, songsMap, true);
        songsMap.forEach((left, right) ->{
            System.err.println("Название: " + left.getSong());
            System.err.println("Исполнитель: " + left.getArtist());
            System.err.println("Альбом: " + left.getAlbum());
            System.err.println("Путь: " + right);
            System.out.println("\n\n");
        } );


//        Mp3File mp3file = new Mp3File("E:\\iTunes Media\\Music\\Bob Seger\\Stranger in Town\\01 Hollywood Nights.m4a");
        //а я вот обратил внимание, к этой песне у нее не было претензий, хотя формат .m4a хм
//        System.out.println("есть в2 тег?: " + (mp3file.hasId3v2Tag() ? "да" : "нет"));
//        ID3v2 id3v2Tag = mp3file.getId3v2Tag();
//        System.out.println("Track: " + id3v2Tag.getTrack());
//        System.out.println("Artist: " + id3v2Tag.getArtist());
//        System.out.println("Title: " + id3v2Tag.getTitle());
//        System.out.println("Album: " + id3v2Tag.getAlbum());
//        System.out.println("Year: " + id3v2Tag.getYear());
//

    }

    // проверяем сходство
    private static void checkCompatibility(){
        // HashMap<ConnectorName, String> lyricsResultMap = new HashMap<>(lyricsMap);

        //массив с удачными песнями
        ArrayList<ConnectorName> songsResultList = new ArrayList<>();
        //массив со всеми пройденными песнями, из него потом вычитаем удачные, и получаем те, что не удались
        ArrayList<ConnectorName> songsFailureList = new ArrayList<>();


        //счетчик записанных
        final int[] countWritten = {0};
        //счетчик требующих проверки в текстовом файле
        final int[] countCheck = {0};

        songsMap.forEach((songsLeft, songsRight) -> {
                //создаем экземпляры наших параметров, чтоб типа меньше нагружалось
            String tempSongSONGS = songsLeft.getSong();
            String tempAlbumSONGS = songsLeft.getAlbum();
            String tempArtistSONGS = songsLeft.getArtist();
                //занолняем список всеми песнями
            ConnectorName tempConnectorObject = new ConnectorName(tempArtistSONGS, tempAlbumSONGS, tempSongSONGS);
            songsFailureList.add(tempConnectorObject);
            lyricsMap.forEach((lyricsLeft,lyricsRight) ->{
                String tempSongLYRICS = lyricsLeft.getSong();
                String tempAlbumLYRICS = lyricsLeft.getAlbum();
                String tempArtistLYRICS = lyricsLeft.getArtist();

                //оооо это самое интересное
                // тут мы вычисляем расстояние левенштейна, это мы, типа, вычисляем насколько должны изменить одну строку, чтобы из нее получилась вторая, так и считается сходство, первой строкой всегда должна быть та, что длиннее
                double songDistance = similarity(tempSongSONGS, tempSongLYRICS);
                // double artistDistance = similarity(tempArtistSONGS, tempArtistLYRICS); //это нам не понадобится
                //теперь нужно подумать, на сколько могут отличаться названия, скажем, если названия одинаковы на 60%+ то добавляем upd 60 оказалось много тут все еще есть проблемы, если какой -нибудь идиот решил выпустить песни с очень похожими названиями, и что еще хуже две части песни ну и ладно
                if (tempArtistSONGS.equals(tempArtistLYRICS) || tempArtistSONGS.equals("Compilations") || tempArtistLYRICS.contains(tempArtistSONGS) || tempArtistSONGS.contains(tempArtistLYRICS)) {
                        if (songDistance>=0.75){
                            if (songDistance<1){
                                // если вероятность <100%, то заносим в список неблагонадежных, после чего выписываем их всех в файл (от них стоит ожидать подвоха) какое дебильное слово подвох
                                countCheck[0]++;
                                writeFile(songsRight, lyricsRight, String.valueOf(countCheck[0]), "СТОИТ ПРОВЕРИТЬ");
                            }
                        // записываем в файл хороших песен
                        System.out.println("EQUALS!!!! ПЕСНЯ: " + songsMap.get(songsLeft) + "    \n ТЕКСТ: " + lyricsMap.get(lyricsLeft));
                        countWritten[0]++;
                            writeFile(tempSongSONGS, tempAlbumSONGS, tempArtistSONGS, lyricsRight + "     " + " ! " + countWritten[0] + " ! ", "ЗАПИСАННЫЕ");
                            //  writeFile(tempSongLYRICS, tempAlbumLYRICS, tempArtistLYRICS, lyricsRight + "     " + " ! " + count3[0] + " ! ", "ЗАПИСАННЫЕ");
                        songsResultList.add(tempConnectorObject);

                            /**
                             * сведение треков) с текстом
                             */
                           connectLyrics(lyricsRight, songsRight);
                        } else if (songDistance>=0.6){
                            //если >=60% вероятность, то добавляем в лист ожидания
                            pendingList.add(new TwoStrings(lyricsRight, songsRight));
                        }

                }
                // проверка эквивалентноти названия песни на 100% + содержится ли имя исполнителя но мне ведь нечем заняться, поэтому сделаем по-круче
//                if (tempSongSONGS.equals(tempSongLYRICS)){
//                    if (tempArtistSONGS.equals(tempArtistLYRICS) || tempArtistLYRICS.contains(tempArtistSONGS)) {
//                        System.out.println("EQUALS!!!! ПЕСНЯ: " + songsMap.get(songsLeft) + "    \n ТЕКСТ: " + lyricsMap.get(lyricsLeft));
//                        count3[0]++;
//                        writeFile(tempSongLYRICS, tempAlbumLYRICS, tempArtistLYRICS, lyricsRight + "     " + " ! " + count3[0] + " ! ", "ЗАПИСАННЫЕ");
//                        songsResultList.add(tempConnectorObject);
//                    }
//                }

            });


        });
            //тут все понятно
        //все
        final int[] countAll = {0};
        //без пары
        final int[] countAbandoned = {0};
        //ожидающие рассмотрения
        final int[] countPending = {0};
            //записываем и выводим
        pendingList.forEach(pendingSong -> {
            countPending[0]++;
            writeFile(pendingSong.getLyricsPath(), pendingSong.getSongsPath(), "", " ! " + countPending[0] + " ! ", "В ОЖИДАНИИ");
        });

        songsFailureList.forEach(song -> {
            countAll[0]++;
            writeFile(song.getSong(), song.getAlbum(), song.getArtist(), " ! " + countAll[0] + " ! ", "ВСЕ");
        });


            //то, о чем я писал выше (вычитаем и получаем список не нашедших пару)
        songsFailureList.removeAll(songsResultList);

        songsFailureList.forEach(song -> {
            countAbandoned[0]++;
           writeFile(song.getSong(), song.getAlbum(), song.getArtist(), " ! " + countAbandoned[0] + " ! ", "НЕ ЗАПИСАЛО");
       });


    }


                    //просто запись в файл
            private static void writeFile(String song, String album, String artist, String path, String fileName){

                try(FileWriter writer = new FileWriter(fileName+ ".txt", true))
                {
                    String text = fileName + ": Песня: " + song + " Альбом: " + album +" Исполнитель: " + artist + "\n\t " +
                            "ПУТЬ: " + path + "\n\t";
                    writer.write(text);
                    writer.flush();
                }
                catch(IOException ex){

                    System.out.println(ex.getMessage());
                }
            }
                    //перегружаем наш метод, для другого вида записей
            private static void writeFile(String songPath, String lyricsPath, String count, String fileName){

                try(FileWriter writer = new FileWriter(fileName+ ".txt", true))
                {
                    String text = "! " + count + "!" + "\nПуть к песне: " + songPath + "\nПуть к тексту: " + lyricsPath +"\n\n";
                    writer.write(text);
                    writer.flush();
                }
                catch(IOException ex){
                    System.out.println(ex.getMessage());
                }
            }

                //так как текст в песне хранится в фреймах, долго рассказывать, короче нам нужно перевести наш файл в одну строку
            private static String readLyrics(String lyricsPath){

                ArrayList<String> lyricsList;
                String lyricsString = "";
                                //это будет мы переводим наш файл в массив строк
                lyricsList = readFile(lyricsPath);
                                //это мы переводим массив строк в одну строку, дальше эта строка попадает на конвеер перевода в массив byte[], но это уже другая история)
                for (int i=0; i<lyricsList.size(); i++){
                    lyricsString+=lyricsList.get(i) + "\n";
                }
            return  lyricsString;
            }


                            //то самое волшебное расстояние левенштейна, встроено в апач коммонс текст, но у меня вроде получился свой, но пока, не правильно работает, поэтому используем этот
    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) {
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0;}

        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        return (longerLength - levenshteinDistance.apply(longer, shorter)) / (double) longerLength;
    }

                //считывание файла в массив строк
    private static ArrayList<String> readFile(String link){
        ArrayList<String> list = new ArrayList<>();
        try{
            FileInputStream albumLinks = new FileInputStream(link);
            BufferedReader br = new BufferedReader(new InputStreamReader(albumLinks));
            String strLine;
            while ((strLine = br.readLine()) != null){

                list.add(strLine);

            }
        } catch (IOException e){
            System.out.println("Ошибка " + e);
        }
        return list;
    }
    //заполняем текстами наши песни
    public static void connectLyrics(String lyricsPath, String songPath){

        Mp3File mp3fileNew = null;
        try {
            mp3fileNew = new Mp3File(songPath);
			obsoleteStage(mp3fileNew);
        } catch (IOException | UnsupportedTagException | InvalidDataException | IllegalArgumentException e) {
            System.err.println("ERROR: "  + e);
            writeFile(songPath, String.valueOf(e), "", "");
            return;
        }
                ID3v2 id3v2Tag = mp3fileNew.getId3v2Tag();
            //проверяем, нет ли у него, случайно, тега lyricsReady. это для дальнейшнего использования, чтобы в следующие разы оно не перезаполняло уже готовые песни
            if (id3v2Tag.getPaymentUrl()==null || !id3v2Tag.getPaymentUrl().equals("lyricsReady")) {

                String lyricsText = readLyrics(lyricsPath);
                id3v2Tag.setLyrics(lyricsText);
                saveMp3(mp3fileNew, songPath);

                //System.out.println("lyricsReady: " + mp3file.getId3v2Tag().getPaymentUrl());
            } else {//выписываем ошибку в лог
                writeFile(songPath,lyricsPath, "есть тег lyricsReady", "lyricsReadyERRORS");
            }
        }


                //сохраняем мп3, там иногда м4а это эпл лосслесс, дурацкий эпл боится использовать флак, думают что найдется владелец фри аудио кодека и засудит их, ну и ладно, мне не сложно переконвертировать
    private static void saveMp3(Mp3File mp3file, String oldPath){
        try {
            if(!mp3file.hasId3v2Tag()){
                return;
            }
            ID3v2 id3v2Tag = mp3file.getId3v2Tag();
            id3v2Tag.setPaymentUrl("lyricsReady");
            mp3file.save(oldPath+".new");
        } catch (IOException | NotSupportedException e) {
            System.out.println("ERROR не может записать новый файл");
            writeFile(String.valueOf(e), oldPath, "ERROR не может записать новый файл", "ERRORS");
        }
        renameFile(oldPath);
    }
        //так как файлы просто так не переименовываются, не знаю почему, но сначала создаем еще один файл, и там все написано, кстати. говорят что все приложения так же изменяют файлы, ладно..
    private static void renameFile(String oldPath){
        //чтоб не запутаться
        //берем НОВЫЙ файл
        File newFile = new File(oldPath + ".new");
        if(!newFile.exists() || newFile.isDirectory()) {
            writeFile(oldPath, "НЕ НАШЛО .new файл, нужно проверить", "", "ERRORS");
        }
        //берем старый файл
        File oldFile = new File(oldPath);
        //копируем путь старого файла
        String buf = oldFile.getPath();
        //берем файл бэкапа
        File bakFile = new File(oldPath + ".bak");
        if(bakFile.exists() && !bakFile.isDirectory()) {
            writeFile(oldPath, "НЕ ЗАПИСАЛО .bak файл, нужно проверить", "", "ERRORS");
            return;
        }
        //переименовываем старый файл в файл бэкапа
        System.out.println(oldFile.renameTo(bakFile));
        //переименовываем новый файл в старый
        System.out.println(newFile.renameTo(new File(buf)));
        //удаляем старый файл(уже бекапа)
        System.out.println(bakFile.delete());

    }
	
	public static void obsoleteStage(Mp3File item){

        if (item.getId3v2Tag().getObseleteFormat()){
            String tempTitle = item.getId3v2Tag().getTitle();
            String tempYear = item.getId3v2Tag().getYear();
            String tempAlbum = item.getId3v2Tag().getAlbum();
            String tempArtist = item.getId3v2Tag().getArtist();
            String tempAlbumArtist = item.getId3v2Tag().getAlbumArtist();
            int tempGenre = item.getId3v2Tag().getGenre();
            String tempGenreDescription = item.getId3v2Tag().getGenreDescription();
            String tempTrack = item.getId3v2Tag().getTrack();
            byte[] tempAlbumCover = item.getId3v2Tag().getAlbumImage();
            String tempMimeType = item.getId3v2Tag().getAlbumImageMimeType();
            String tempLyrics = item.getId3v2Tag().getLyrics();
            String tempLyricsReady = item.getId3v2Tag().getPaymentUrl();

            ID3v24Tag id3v24Tag = new ID3v24Tag();
            item.setId3v2Tag(id3v24Tag);

            item.getId3v2Tag().setTitle(tempTitle);
            item.getId3v2Tag().setYear(tempYear);
            item.getId3v2Tag().setAlbum(tempAlbum);
            item.getId3v2Tag().setArtist(tempArtist);
            item.getId3v2Tag().setGenre(tempGenre);
            item.getId3v2Tag().setGenreDescription(tempGenreDescription);
            item.getId3v2Tag().setTrack(tempTrack);
            item.getId3v2Tag().setAlbumImage(tempAlbumCover, tempMimeType);
            item.getId3v2Tag().setAlbumArtist(tempAlbumArtist);
            item.getId3v2Tag().setLyrics(tempLyrics);
            item.getId3v2Tag().setPaymentUrl(tempLyricsReady);

          //  item.getId3v2Tag().setArtist("ARTIST NEW OBSOLETE");

            saveMp3(item, item.getFilename());

        }
//        else {
//            //  System.out.println(item.getId3v2Tag().getGenre() + "  " + item.getId3v2Tag().getGenreDescription());
//            item.getId3v2Tag().setArtist("ARTIST NEW not obsolete");
//            saveMp3(item, item.getFilename());
//        }
}


    }
        //о уже конец

