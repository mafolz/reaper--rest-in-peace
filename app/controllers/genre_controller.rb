class GenreController < ApplicationController
  # Ein Genre wird einem Artisten zugeordnet
  layout "none"
  before_filter :logon
  
  # Für REST alle Genre als XML angeben
  # GET /Genres.xml 
  def index
    @genres=Genre.find(:all)
    respond_to do |format|
      format.xml { render :xml => @genres.to_xml }
    end   
  end
  
  def update
    
  end
  # REST ein element über ID anzeigen
  # GET /Genres/1.xml 
  def show
    @genre = Genre.find(params[:id])
    respond_to do |format|
      format.html
      format.xml { render :xml => @genre.to_xml }
    end    
  end
  
  def edit
    @genre=Genre.find(params[:id])
    # Wenn Formulardaten vorhanden sind beginne
    # Ansonsten nur Formular anzeigen
    if ! params[:genre].nil? and hasRole "tagger"
      @genre.name=params[:genre][:name]
      if(@genre.valid?)
        @genre.save()
        redirect_to :controller=>"music"
      end
    end    
  end
  
  def destroy
    @genre=Genre.find(params[:id])
    if hasRole "tagger"
      @genre.destroy
    end
    redirect_to :controller=> "music"    
  end
  
  def addArtist
    @genre=Genre.find(params[:id])
    # Wenn Formulardaten vorhanden sind beginne
    # Ansonsten nur Formular anzeigen
    if ! params[:artist].nil? and hasRole "tagger"
      @artist=@genre.artists.new(params[:artist])
      @artist.save()
      #@artist=true
    end
  end
end
