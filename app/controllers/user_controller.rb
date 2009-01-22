class UserController < ApplicationController
  
  # Login-Methode
  # Prüft ob Benutzername und Passwort stimmen und leitet weiter
  def login
    @user=User.find_by_name_and_password(params[:login][:name],params[:login][:password])
    if( not @user.nil? )
      # Wenn benutzer keine Berechtigung hat
      if @user.access== "leecher"
        flash['login']= @user.Name+" hat keine Berechtigung!<br/>
                                     Warten sie bis ein Admin ihnen diese zuteilt."
        redirect_to :controller => "login"
      else
        session[:logon]= true;
        session[:user]=params[:login][:name]
        redirect_to :controller => "streamripper"
      end
    else 
      reset_session
      flash['login']= "Benutzer nicht bekannt, oder falsches Passwort"
      redirect_to :controller => "user"
    end
  end
  
  # Indexmethode
  # für REST /login.xml?user=name&pw=pw ein login
  def index
    respond_to do |format|
      # Für HTML
      format.html do        
        # wenn eingelogt leite weiter zum Streamripper Panel
        if session[:logon]
          redirect_to :controller => "streamripper"
        else          
          reset_session
        end
      end
      # Für REST
      format.xml do        
        @user=User.find_by_name_and_password( params[:user],params[:pw])
        # Wenn Benutzer gefunden wird
        if not @user.nil?
          session[:logon]= true;
          session[:user]=params[:user]
          render :xml => @user.to_xml(:except=>[:id,:created_at,:updated_at,:password])
        else
          # Sonst setze Session zurück
          reset_session
        end
      end
    end      
  end
  
  # Ausloggen
  def out
    reset_session
    redirect_to :controller => "login"    
  end
  
  # Neuen User anlegen
  # REST POST /user.xml
  def create
    # Wenn Formular abgeschickt
    if ! params[:user].nil?
      if params[:user][:password] == params[:user][:password2] 
        # Wird gesetzt damit die Create-Forms verschwinden
        @inputs = true; 
        # entferne Passwortwiederhohlung aus den Parametern um User zu erzeugen
        params[:user].delete(:password2)        
        # Erster Benutzer muss Admin sein
        if User.find(:all).length == 0
          params[:user][:access]="admin"
        else
          params[:user][:access]="leecher"
        end
        @newuser =  User.new(params[:user])
        # @created wird gesetzt zur erstellungsmessage, da komischerweise ein
        # zugriff auf @newuser.valid? nicht fruchtet
        if @newuser.valid?
          @created=true
          # Wenn user Admin ist,(erste anmeldung) setze die Variable un erstelle ein basis genre
          if params[:user][:access]=="admin"
            @admin = true
            Genre.create(:name => "unknown")
          end
        end
        @newuser.save
      else        
        # Wenn diese Variable nil ist, sind beide Passwörter nicht gleich
        @bothpw=true
      end    
    end
    respond_to do |format|
      format.html  
      format.xml       
    end
  end
end
