require 'fileutils'
require 'get_i_d3'
  
class Artist < ActiveRecord::Base
  belongs_to :genre
  belongs_to :path
  has_many  :songs,:dependent=>:destroy,:order=>"title"
  has_many  :albums, :dependent=>:destroy,:order=>"title"
  
  validates_presence_of :name,:path_id, :genre_id
  
  def validate
    # Kontrollieren ob das angegebene Genre wirklich vorhanden ist
    if self.genre.nil?
      errors.add(:Artist, "zugewiesenes Genre nicht vorhanden")
      return false
    end
    
    temp = self.path.uri   
    FileUtils.cd( temp )
    # Wenn der Artist im Pfad nicht als Verzeichnis exisitert wird dieses angelegt
    if ! File.exist?( self.name)
      FileUtils.mkdir( self.name )
    end
    # Wenn der Artist kein Verzeichnis ist
    if ! File.directory?( self.name )
      errors.add(:Artist, " Name exisitert schon als Datei, muss aber Verzeichnis sein!") 
      return false
    end    
  end
  
  def validate_on_create
    #self.Name=self.Name.strip
    # Schauen ob der Artist schon erstellt wurde
    if Artist.exists?(:name=> self.name)
      errors.add(:Artist, "vorhanden") 
      return false
    end
  end
  
  # Sucht in dem Ordner nach Songs
  def scan_songs
    temp = self.path.uri
    FileUtils.cd( temp )  
    # Füge für jeden existierende mp3 ein Song-Objekt hinzu
    Dir.foreach(self.name) do |file|
      if file !="." and file != ".."
        parsingName = file.scan( /(.*) - (.*)\.(.{3})/)[0]
        if (!parsingName.nil?) 
          self.songs.create(:title=>parsingName[1], :format=>parsingName[2], :localFlag=>true)
        end
      end
    end      
  end

  #
  def self.detect_genre( name )
    scan = GetID3.new
    genre = scan.get_genre(name)
    
    if Genre.exists?(:name => genre)
      return  Genre.find_by_name(genre)
    end
    genre = Genre.new(:name => genre)
    genre.save!
    return  genre
  end
  
  # Artisten URI
  def uri    
    temp = self.genre.path.uri
    return File.join(temp,self.name)
  end
  
  # Setzt neuen Namen, und falls der Artist schon vorhanden ist, werden
  # alle songs zu diesem Verschoben un der jetzige Artist zerstört
  def changeName newname
    temp = self.path.uri   
    FileUtils.cd( temp )
    # Wenn der neue Name schon als Artist existiert
    if Artist.exists?(:name=>newname) 
      puts "Test 1"
      # Songs in dein existeirenden Artist verscheiben
      self.songs.each do |song|
        # Kompletter wechsel des Artisten
        puts song.title
        song.changeArtist( Artist.find_by_name(newname).id )
      end
      # Songs dem Artisten zuordnen
      Artist.find_by_name(newname).songs << self.songs
      # Leeren Artistenordner löschen
      begin
        FileUtils.rmdir(self.uri)
      rescue Exception=>e
        puts e
      end
      self.destroy
    else
      self.songs.each do |song|
        # Nur Artistnamen ändern
        puts song.title
        song.editArtist( newname )
      end
      # Leeren Artistenordner löschen
      begin
        FileUtils.rmdir(self.uri)
      rescue Exception=>e
        puts e
      end
      self.name  =  newname
      self.save
    end
  end
  
#  # Ändert das Genre, wenn das Genre einen anderen Pfad hat, wird hier verschoben!
#  def changeGenre id
#    self.genre=Genre.find(id)
#    # wenn der Pfad der gleiche ist muss nicht verschoben werden
#    if genre.path == self.genre.path
#      self.genre=genre
#    else # Ansonsten wird verschoben
#      FileUtils.mv(self.uri, File.join(genre.path.uri,self.name))
#      self.genre=genre
#    end
#    self.save!
#  end

end
