public class ConnectorName {

    String artist;
    String album;
    String song;

    ConnectorName(String artist, String album, String song){
           this.artist=artist;
           this.album=album;
           this.song=song;
        }


    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getSong() {
        return song;
    }
}
