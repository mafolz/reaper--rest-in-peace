class Song < ActiveRecord::Base
  belongs_to :artist
  validates_presence_of :title,:format,:artist_id
  validates_format_of :format, :with => /\A(mp3)|(MP3)|(ogg)|(OGG)\Z/
  
  def validate
    if self.localFlag
      temp = self.artist.genre.path.uri    
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
    temp = self.artist.genre.path.uri
    fulldir = File.join(temp, self.artist.name, self.filename)
    puts fulldir
    puts File.exists?(fulldir)
    return fulldir
  end  
  
  def filename
    return  self.artist.name+" - "+self.title+"."+self.format 
  end
  
  # ändert den Artistennamen und die damit betroffene Dateistruktur
  # Diese methode wird aufgerufen wenn der Artistenname sich ändert, 
  # nicht wenn der Artist selbst ausgewechselt wird, hierfür ist changeArtist da
  def editArtist artistname
    temp = self.artist.genre.path.uri
    # Wenn das Artistenverzeichniss noch nicht da ist
    FileUtils.cd( temp )
    if ! File.exists?(artistname)
      FileUtils.mkdir(artistname)
    end
    # Verschiebe vom jetzigen URI zum neuen Artist 
    FileUtils.mv(self.uri, 
                 File.join(temp,artistname,artistname+" - "+self.title+"."+self.format ))
  end
  
  def changeArtist id
    newArtist=Artist.find(id)
    tempuri=self.uri
    self.artist=newArtist
    self.save!
    # Verschiebe vom jetzigen URI zum neuen Artist 
    FileUtils.mv(tempuri, 
                 File.join(newArtist.uri,artistname+" - "+self.title+"."+self.format ))
  end
  
  def changeTitle titlename
    temp = self.artist.genre.path.uri
    FileUtils.mv(self.uri, File.join(temp, self.artist.name,self.artist.name+" - "+titlename+"."+self.format ))
    self.title=titlename
    self.save!
  end
end
