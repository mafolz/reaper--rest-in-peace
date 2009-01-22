# Plugin aus http://wiki.rubyonrails.org/rails/pages/HowToRunBackgroundJobsInRails
class ActionController::Base
  @scan

  # Startet den Thread
  def scan( path )
    # Stoppen des Threads bevor er ausgeführt wird
    self.stop()

# TODO Doesnt work
    @scan = Thread.start( path )  do
      # Scan the existing MP3's and tag them
      path.scan_uri() 
      path.scan_artists()
    end
  end

  def stop
    # Falls Thread noch greifbar, beende ihn
    if ! @scan.nil?
      @scan.terminate()
    end
  end
end