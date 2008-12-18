class Streamripper < ActiveRecord::Base
  belongs_to :radiostation
  
  # Wenn kein Streamripper Objekt vorhanden ist, erzeuge es
  if Streamripper.find(:all).length == 0
    # Wenn kein Radio vorhanden ist, erzeuge eines
    if Radiostation.find(:all).length == 0
      Radiostation.create(:address=>"http://www.shoutcast.com/sbin/shoutcast-playlist.pls?rn=30284\&file=filename.pls")
    end
    Streamripper.create(:radiostation_id=>Radiostation.find(:last,:order=>"updated_at").id)
  end
  

end
