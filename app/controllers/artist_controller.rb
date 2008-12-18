class ArtistController < ApplicationController
  # Repräsentiert den Interpreten
  # Hat ein Genre, und kann nur in einem Pfad gleichzeitig
  # exisiteren.
  layout "none"
  before_filter :logon
  
  # Für REST alle artisten als XML angeben
  def index
    respond_to do |format|
      format.xml { render :xml => Genre.find(params[:Genre_id]).artists.to_xml }
    end    
  end
  
  # REST ein element über ID anzeigen
  def show
    @artist = Artist.find(params[:id])
    
    respond_to do |format|
      format.html
      format.xml { render :xml => @artist.to_xml }
    end    
  end
  
  def edit
    @artist = Artist.find(params[:id])
    # Wenn Formulardaten vorhanden sind beginne
    # Ansonsten nur Formular anzeigen
    if ! params[:artist].nil? and hasRole "tagger"
      @artist.changeName( params[:artist][:name] )
    end    
  end
  
  # zerstört Artisten und dessen Verzeichnis
  def destroy
    @artist = Artist.find(params[:id])
    if hasRole "tagger"
      FileUtils.rmdir(@artist.uri)
      @artist.destroy
    end
    redirect_to :controller=> "music"
  end
  
  # Artisten verschieben
  def moveTo
    newgenre=Genre.find_by_name(params[:artists][:genre])
    genre=Genre.find(params[:id])
    if hasRole "tagger"
      # Jedes Checkbox Element durchgehen ob es true ist
      genre.artists.each do |artist|
        if params[:artists][artist.id.to_s]== "1"
          artist.changeGenre(newgenre.id)
        end
      end
    end
    redirect_to :controller=> "music"
  end
end
