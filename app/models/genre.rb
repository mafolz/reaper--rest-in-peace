class Genre < ActiveRecord::Base
  belongs_to :path#,  :foreign_key => "path_id"
  has_many :artists,:dependent=>:destroy, :order=>"name"
  
  validates_presence_of :name, :path_id
  
  def validate
    #self.Name=self.Name.strip
    # Schauen ob das genre schon erstellt wurde
    if Genre.exists?(:name=> self.name)
      errors.add(:Genre, "Genre schon vorhanden") 
      return false
    end
    # Kontrollieren ob der Angegebene Pfad exisitiert
    if self.path.nil?
      errors.add(:Genre, "angegebener Pfad nicht vorhanden") 
      return false
    end
  end
  
  def scanArtists
    self.artists.each do |artist|
      artist.scanSongs()
    end
  end
end
