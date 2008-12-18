class SettingsController < ApplicationController
  before_filter :logon, :admin
  # benötigte Programme
  # apt-get libid3-dev , streamripper, last.fm proxy
  
  # Benutzer Zugriffsrechte setzen
  def userAccess
    @user=User.find(params[:id])
    # Wenn Formulardaten vorhanden sind beginne
    # Ansonsten nur Formular anzeigen
    if ! params[:user].nil? and hasRole "admin"
      @user.access=params[:user][:access]
      @user.save()
    end  
  end
  
  # Benutzer löschen
  def userDestroy
    if  User.find(params[:id]).Name != session[:user] and hasRole "admin"
      User.find(params[:id]).destroy
    end
    redirect_to :controller=> "settings"  
  end
  
  # Pfad neu einlesen
  def path_reload
    temp= Path.find(params[:id])
    uri = temp.uri
    name = temp.name
    temp.destroy
    redirect_to :controller=>"path" , :action=>"new", :path=>{ :name=>name, :uri => uri}
    
  end
  
  # Server erstellen
  def newServer
    # Wenn Formulardaten vorhanden sind beginne
    # Ansonsten nur Formular anzeigen
    if ! params[:server].nil? and hasRole "admin"
      @server= Server.new(params[:server])
      @server.save()
    end      
    render :layout => false
  end
  
  # Server löschen
  def serverDestroy
    if hasRole "admin"
      Server.find(params[:id]).destroy
    end
    redirect_to :controller=> "settings"  
  end
  
end
