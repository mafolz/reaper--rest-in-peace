##
# ==
#
# Author:: mafolz
class Album < ActiveRecord::Base
  belongs_to :artist
  has_many  :songs,:dependent=>:destroy,:order=>"title"
  
  validates_presence_of :title,:artist_id

  def validate
    if self.artist.nil?
      errors.add(:Album, "zugewiesener Artist nicht vorhanden")
      return false
    end
    return true
  end
end
