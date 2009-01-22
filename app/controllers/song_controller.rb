class SongController < ApplicationController
  # Repräsentiert einen Song.
  # Lokale Songs liegen in MP3's vor, besitzen einen Artist
  # welcher zugleich der Ordner ist, in dem die MP3's liegen
  before_filter :logon  
  
  
  # Für REST alle artisten als XML angeben
  def index
    respond_to do |format|
      format.xml { render :xml => Artist.find(params[:Artist_id]).songs.to_xml }
    end    
  end
  
  # REST ein element über ID anzeigen/downloaden
  def show
    @song = Song.find(params[:id])
    respond_to do |format|
      format.html
      format.xml { render :xml => @song.to_xml }
      format.mp3 do 
        send_file  @song.uri 
      end
    end    
  end
  
  # Javascript-Workarround zur Korrekten MP3 Anzeige.
  # Dies ist für Erweiterungen wie z.B. songbird's javascripting Engine sehr wichtig
  def listen
    parsing_name = params[:id].scan( /(.*) - (.*)/)[0]
    puts "\n\n"+ parsing_name.inspect
    @artist = Artist.find_by_name(parsing_name[0])
    puts "\n\n"+ @artist.inspect
    @title = parsing_name[1]
    puts "\n\n"+ @title.inspect
    song = Song.find_by_title(@title)#, :conditions => "artist_id = #{@artist.id} ")
    respond_to do |format|
      format.mp3 do
        send_file  song.uri
      end
    end
  end
  
  def edit
    @song = Song.find(params[:id])
    # Wenn Formulardaten vorhanden sind beginne
    # Ansonsten nur Formular anzeigen
    if ! params[:song].nil? and hasRole "tagger"
      @song.changeTitle(params[:song][:title])
    end    
  end
  
  # Artisten verschieben
  def moveTo
    newartist=Artist.find_by_name(params[:songs][:artist])
    artist=Artist.find(params[:id])
    if hasRole "tagger"
      # Jedes Checkbox Element durchgehen ob es true ist
      artist.songs.each do |song|
        if params[:songs][song.id.to_s]== "1"
          song.changeArtist(newartist.id)
        end
      end
    end
    redirect_to :controller=> "music"
  end
end
