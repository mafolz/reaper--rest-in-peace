class Genre < ActiveRecord::Base
  #belongs_to :path#,  :foreign_key => "path_id"
  has_many :artists,:dependent=>:destroy, :order=>"name"
  
  validates_presence_of :name
  
  def validate
    #self.Name=self.Name.strip
    # Schauen ob das genre schon erstellt wurde
    if Genre.exists?(:name=> self.name)
      errors.add(:Genre, "Genre schon vorhanden") 
      return false
    end
  end
  
  def scan_artists
    self.artists.each do |artist|
      artist.scanSongs()
    end
  end
end
