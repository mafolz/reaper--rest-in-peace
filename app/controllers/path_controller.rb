require 'path_scan_thread'
class PathController < ApplicationController
  # Ein Pfad repräsentiert einen Physischen Pfad im Dateisystem
  # des Servers. Er enthält mehrere Genres und Artisten 
  layout "none"
  before_filter :logon
  
  # pfad erstellen
  def new
    # Wenn Formulardaten vorhanden sind beginne
    # Ansonsten nur Formular anzeigen
    if ! params[:path].nil? and hasRole "admin"
      @pfad=Path.new(params[:path])
      if @pfad.valid?
        @pfad.save
        # Pfad nach Artisten untersuchen
        
        @pfad.scan_uri() 
        @pfad.scan_artists()
        #self.scan( @pfad )
      end
    end
  end  
  # genre hinzufügen
  def addGenre
    # Wenn Formulardaten vorhanden sind beginne
    # Ansonsten nur Formular anzeigen
    if ! params[:genre].nil? and hasRole "tagger"
      @pfad=Path.find(params[:id])
      @genre=@pfad.genres.create(params[:genre])
    end
  end
  
  def destroy
    @pfad=Path.find(params[:id])
    if hasRole "tagger"
      @pfad.destroy
    end
    redirect_to :controller=> "settings"    
  end
  
  def edit
    @pfad=Path.find(params[:id])
    # Wenn Formulardaten vorhanden sind beginne
    # Ansonsten nur Formular anzeigen
    if ! params[:path].nil? and hasRole "tagger"
      @pfad.name=params[:path][:name]
      @pfad.save()
    end    
  end
end
