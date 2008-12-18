require 'id3lib'

class Path < ActiveRecord::Base
  has_many :genres,:dependent=>:destroy,:order=>"name"
  
  require 'fileutils'
  validates_presence_of :uri
  validates_presence_of :name
  
  # Prüft ob der Name besetzt ist
  def validate_on_update
    if Path.exists?(:name => self.name)
      errors.add(:Path, "Name exisitiert schon") 
      return false
    end
  end
  # Prüfen ob das Element ein Unikat ist und ob 
  # der angegebene pfad ein Verzeichnis ist
  def validate_on_create
    if Path.exists?(:name => self.name) or Path.exists?(:uri => self.uri)
      errors.add(:Path, "wurde schon hinzugefügt") 
      return false
    end
    if ! File.exist?( self.uri )
      errors.add(:Path, "exisitert nicht") 
      return false
    end
    if ! File.directory?( self.uri )
      errors.add(:Path, "ist eine Datei") 
      return false
    end
  end
  
  
  # Scannt den Ordner nach Interpreten/Artisten
  # und fügt sie mit dem letzten gefundenen Genre hinzu
  def scanURI
    # Wenn dem pfad noch kein Genre zugewiesen wurde, erstelle ein Genre 
    # mit dem Pfadnamen
    if self.genres.empty?
      self.genres.create(:name=>self.name)
    end
    # Wechsle in den Pfad
    FileUtils.cd( self.uri )
    # Füge für jeden existierenden Ordner einen Artisten hinzu
    Dir.foreach(self.uri) do |subdir|
      if subdir !="." and subdir != ".."
        localGenre = self.genres.find(:last)
        begin
          # Genre rausfinden          
          FileUtils.cd( subdir )        
          tag = ID3Lib::Tag.new(Dir.glob("*.mp3")[0], ID3Lib::V_ALL)
          genreCache = self.genres.find(:last).name
          # lese genre Frame aus
          tag.each do |frame|
            if frame[:id] == :TCON
              # Korregiere unschöne UTF Fehler
              genreCache = frame[:text].toutf8
              if genreCache =~ /\(\d*\)/                
                genreCache = ID3Lib::Info::Genres[genreCache.sub(/(\()|(\))/,'').to_i]
              end
            end
          end
          puts genreCache
          # Wenn genre im Pfad exisitiert
          if self.genres.find_by_name( genreCache) != nil
            localGenre= self.genres.find_by_name( genreCache)
          # Sonst schaue ob es überhaupt existiert, wenn nein, füge neu ein
          elsif Genre.find_by_name( genreCache) == nil
            localGenre= self.genres.create(:name => genreCache)
          end
        rescue Exception => boom
          puts "fehler "+ boom
        end
        # Rückspringen wegen Tagauslese
        FileUtils.cd( self.uri )
        self.genres.find(localGenre.id).artists.create( :name=>subdir)
      end
    end    
  end
  
  def scanGenres    
    self.genres.each do |genre|
      genre.scanArtists()
    end
  end
end
