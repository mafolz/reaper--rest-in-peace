class MusicController < ApplicationController
  before_filter :logon
  def index
    redirect_to :action => "browse"
  end
  
  def remoteArtist
    render :layout => "none"
    @server=Server.find(params[:server_id])
    RemoteArtist.setServer(@server)
    #  Hier leigt der HUnd begraben, aber wieso?
    @remoteArtists=RemoteArtist.find(:all, :params=>{:Genre_id=>params[:id].to_i})
  end
  
  # Startanzeige der iTunes ansicht
  def browse
    @genres = Genre.find(:all,  :order=>"name")
    @artists = Artist.find(:all,  :order=>"name")
    @albums = Album.find(:all,  :order=>"title")
    @songs = Song.find(:all, :order=>"title")
    get_song_ids
  end
  
  # Update der Artist-liste
  def browse_artists
    @albums = []
    @artists = []
    @songs = []
    
    # Get the String from Params and split them into a Array for
    # Search Genres with these Indices
    param_array = params[:genres].split(',')
    @genres = Genre.find(param_array)
    
    
    @genres.each do |genre|      
      @artists += genre.artists
    end
    
    @artists.each do |artist|
      @songs += artist.songs
      @albums += artist.albums
    end
    get_song_ids
  end
  
  # Update der Song-liste
  def browse_album
    @artists = []
    @songs = []
    @albums = []
    
    
    # Get the String from Params and split them into a Array for
    # Search Artists with these Indices
    param_array = params[:artists].split(',')
    @artists = Artist.find( param_array )
    
    if @artists.class != Array
      test = @artists
      @artists = [test]
    end
    
    @artists.each do |artist|
      @songs += artist.songs
      @albums += artist.albums
    end
    
    get_song_ids
  end
  
  def browse_songs    
    @artists = []
    @songs = []
    
    
    # Get the String from Params and split them into a Array for
    # Search Albums with these Indices
    param_array = params[:albums].split(',')
    @albums = Album.find( param_array )
    
    @albums.each do |album|
      @songs += album.songs
      @artists << album.artist
    end 
    get_song_ids
  end
  
  # This Method will be used to display songs via a AJAX request
  def show_songs
    
      render :text => params.inspect
    
    #render :partial => "select"
  end
  
  private
  def get_song_ids
    @ids = []
    @songs.each do |song|
      @ids << song.id
    end
  end
end
