require 'StreamripperThread'
class StreamripperController < ApplicationController
  before_filter :logon  
  
  def index
  end
  def status       
  end
  def history
  end
  
  # Starten / Neustarten dens Streamripperthreads
  def toggle
    self.start
    redirect_to :controller=>"streamripper"
  end
  # Beenden des Prozesses
  def kill
    self.stop
    redirect_to :controller=>"streamripper"
  end
  
  # Fügt die MP3s aud dem Streamripeprverzeichnis
  # in die Musikdatenbank ein
  def merge  
    FileUtils.cd(File.join("public", "mp3")) 
    Dir["*.mp3"].each() do |file|
      temp    =   file.split(/ - /,2)
      @artistname = temp[0]
      @title  =   temp[1].split(/\./)[0]
      @format =   temp[1].split(/\./)[1]
      # Wenn Artist vorhanden
      if Artist.exists?(:name=>@artistname )
        @artist=Artist.find_by_name(@artistname)
        # Wenn Song schon vorhanden, verschiebe in Dupletten
        if @artist.song.exists?(:name=>@name)
          FileUtils.mv(file, File.join("duplette",file))
        # Sonst verschiebe mp3 und erstelle Song-Objekt
        else
          FileUtils.mv(file, @artist.uri)
          Song.create(:artist_id=> @artist.id,
                      :title=>@title,
                      :format=>@format)
        end
      end
    end
    redirect_to :controller=>"streamripper"
  end  
  
  # Leert das Streamripeprverzeichnis
  def clear  
    FileUtils.cd(File.join("public", "mp3")) 
    Dir["*.mp3"].each() do |file|
      file.delete
    end
    redirect_to :controller=>"streamripper"
  end
  
  
  # Dialog zum Stationswechsel
  def changeStation
    render :layout =>"popup"
    if !params[:radiostation].nil?
      Radiostation.create(params[:radiostation])
    end
  end
  
  def setStation
    @ripper=Streamripper.find(:last)
    @ripper.radiostation=Radiostation.find(params[:id])
    @ripper.save()
    render :inline=>'<script type="text/javascript">window.close()</script>'    
  end
end
