require 'get_i_d3'
require 'id3lib'
require 'fileutils'

class Song < ActiveRecord::Base
  belongs_to :artist
  belongs_to :album
  validates_presence_of :title,:format,:artist_id
  validates_format_of :format, :with => /\A(mp3)|(MP3)|(ogg)|(OGG)\Z/
  
  # Construktor überschreiben
  def create
    super
    # ID3-Tags
    self.set_tags
  end
  
  def validate
    if self.localFlag
      temp = self.artist.path.uri    
      # In den Hauptpfad wechseln
      FileUtils.cd( temp )  
      # In den Artistenpfad wechseln
      FileUtils.cd( artist.name )
      if ! File.exist?( self.artist.name+" - "+self.title+"."+self.format )
        errors.add(:Song,"konnte nicht gefunden werden")
        return false
      end
    end
  end
  
  def uri
    # if the directory isn't present in the cache
    if self.location.nil?
      temp = self.artist.path.uri
      fulldir = File.join(temp, self.artist.name, self.filename)
      puts fulldir
      puts File.exists?(fulldir)
      self.location = fulldir 
      self.save
      return fulldir
    else
      self.location
    end
  end  
  
  def filename
    return  self.artist.name+" - "+self.title+"."+self.format 
  end
  
  def set_tags
    # TODO Make a Loggersystem
    begin
      # ID3-tags aus LastFM
      tag_array = GetID3.new.get_tags( self.artist.name, self.title )
      #
      tag = ID3Lib::Tag.new(self.uri) #, ID3Lib::V_ALL)
      #tag.strip!
      tag.title = self.title
      tag.artist = self.artist.name
      tag.album = tag_array[:album]
      
      # Wenn Album schon dem Artisten hinzugefügt wurde
      unless self.artist.albums.find_by_title( tag.album ).nil?
        self.artist.albums.find_by_title( tag.album ).songs << self
      else
        self.artist.albums.create( :title => tag.album ).songs << self
      end
      unless tag.album == "unknown"
        tag.track = tag_array[:track]
        tag.genre = self.artist.genre.name
        puts tag_array[:cover] 
        response  = Net::HTTP.get_response(URI.parse( tag_array[:cover] ))
        puts response.inspect
        case response
          when Net::HTTPOK  # => Wenn HTTP-Response in Ordnung
          cover = {
            :id          => :APIC,
            :mimetype    => 'image/jpeg',
            :picturetype => 3,
            :description => 'A pretty picture',
            :textenc     => 0,
            :data        => response.body
          }
          tag << cover
        end
      end
      tag.update!
    rescue
      puts "Error rised"
    end
    
  end
  
  
  # ändert den Artistennamen und die damit betroffene Dateistruktur
  # Diese methode wird aufgerufen wenn der Artistenname sich ändert, 
  # nicht wenn der Artist selbst ausgewechselt wird, hierfür ist changeArtist da
  def editArtist artistname
    temp = self.artist.path.uri
    # Wenn das Artistenverzeichniss noch nicht da ist
    FileUtils.cd( temp )
    if ! File.exists?(artistname)
      FileUtils.mkdir(artistname)
    end
    # Verschiebe vom jetzigen URI zum neuen Artist 
    FileUtils.mv(self.uri, 
                 File.join(temp,artistname,artistname+" - "+self.title+"."+self.format ))
  end
  
  # Artisten ändern
  def changeArtist id
    newArtist=Artist.find(id)
    tempuri=self.uri
    self.artist=newArtist
    self.save!
    # Verschiebe vom jetzigen URI zum neuen Artist 
    FileUtils.mv(tempuri, 
                 File.join(newArtist.uri,artistname+" - "+self.title+"."+self.format ))
  end
  
  # Titel ädern
  def changeTitle titlename
    temp = self.artist.path.uri
    FileUtils.mv(self.uri, File.join(temp, self.artist.name,self.artist.name+" - "+titlename+"."+self.format ))
    self.title=titlename
    self.save!
  end
end
