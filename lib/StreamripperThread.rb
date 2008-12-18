# Plugin aus http://wiki.rubyonrails.org/rails/pages/HowToRunBackgroundJobsInRails
class ActionController::Base
  @ripper
  
  # Startet den Stremaripper in einem Thread
  def start
    # Letztes Konfig-Model hohlen
    # Momentan nur max 1 Objekt, aber
    last=Streamripper.find(:last)    
    # Stoppen des Threads bevor er ausgeführt wird
    self.stop()
    
    @ripper=Thread.start(last, os )  do
      if os == "linux"
        system( 'streamripper '+ last.radiostation.address+
                  ' -d '+ File.join(FileUtils.pwd,"public","mp3")+' -s')
      end
    end
  end
  
  def stop
    # Falls Thread noch greifbar, beende ihn
    if !@ripper.nil?
      @ripper.terminate()
    end
    # Sonst benutze Holzhammermemthode für jedes OS
    if os == "linux"
      system('killall streamripper')
    end
  end
end