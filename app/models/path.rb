require 'id3lib'
require 'fileutils'
require 'get_i_d3'

class Path < ActiveRecord::Base
  has_many :genres,:order=>"name"
  has_many :artists,:dependent=>:destroy,:order=>"name"
  
  validates_presence_of :uri
  validates_presence_of :name
  
  # Prüft ob der Name besetzt ist
#  def validate_on_update
#    if Path.exists?(:name => self.name)
#      errors.add(:Path, "Name exisitiert schon")
#      return false
#    end
#  end
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
  def scan_uri
    # Wenn dem pfad noch kein Genre zugewiesen wurde, erstelle ein Genre 
    # mit dem Pfadnamen
    #    if self.genres.empty?
    #      self.genres << Genre.find(0)
    #    end
    # Wechsle in den Pfad
    FileUtils.cd( self.uri )
    # Füge für jeden existierenden Ordner einen Artisten hinzu
    Dir.foreach(self.uri) do |subdir|
      # Aussortieren des Vaterordners und des Aktuellen um Loops zu unnterbinden
      if subdir !="." and subdir != ".."

        # wenn kein Genre Angelegt wurde
        if Genre.find(:first).nil?
          Genre.create(:name => "unknown")
        end
        
        # genrevariable
        really_genre  = Genre.find(:first)

        # Erst über LastFM Genre herrausfinden
        if Artist.detect_genre( subdir ).nil?

          # Genre über ID3-Tags rausfinden
          FileUtils.cd( subdir )
          tag = ID3Lib::Tag.new(Dir.glob("*.mp3")[0], ID3Lib::V_ALL)
          # Fallback über unknown Genre
          genreCache = Genre.find(:first).name

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

          # Tags-Ergebnis als Genre zurückgeben
          unless Genre.find_by_name( genreCache).nil?
            # Wenn das genre schon vorhanden ist
            really_genre = Genre.find_by_name( genreCache )
          else
            # Sonst erstelle es
            really_genre = Genre.create( genreCache)
          end

        else
          # Wenn last-FM ein Genre Gefunden hatt
          really_genre = Artist.detect_genre( subdir )
        end
        puts really_genre.inspect

        # Rückspringen wegen Tagauslese
        FileUtils.cd( self.uri )
        # Genres zuordnen
        self.genres << really_genre
        artist = Artist.new( :name => subdir, :path_id => self.id)
        artist.genre = really_genre
        artist.save
        self.artists << artist
      end

    end    
  end

  # Deprecated
  def scan_genres
    self.genres.each do |genre|
      genre.scan_artists()
    end
  end

  def scan_artists
    self.save!
    self.artists.each do |artist|
      artist.scan_songs()
    end
  end
end
