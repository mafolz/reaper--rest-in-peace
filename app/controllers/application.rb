# Filters added to this controller apply to all controllers in the application.
# Likewise, all the methods added will be available for all controllers.

class ApplicationController < ActionController::Base
  helper :all # include all helpers, all the time
  
  
  # See ActionController::RequestForgeryProtection for details
  # Uncomment the :secret if you're not using the cookie session store
  protect_from_forgery  :secret => 'dd7b24d16555f9b67cc26a2dce2405a9'
  
  # Fehlerbehandlung mit log-Konsole(errorstack)
  # session[:message] kann lokal in Div-Tags ausgegeben werden
  def raiseError message
    if session[:errorstack]== nil
      session[:errorstack]= ""
    end
    session[:errorstack]+= "<b>"+ (Time.now().to_s) +":</b>\t"+message+"<br/>"
    session[:message]= message;
  end  
  
  # Logon-Status garantieren!
  # before_filter :logon muss dafür im Controller defineirt werden
  def logon
    # Wenn nicht eingelogt
    if  session[:logon]==false or 
      session[:logon]== nil or
      User.find_by_name(session[:user]).access =="leecher"
      
      # Über REST einloggen
      case request.format
        when Mime::XML
        authenticate_or_request_with_http_basic do |user, pw| 
          @user=User.find_by_name_and_password(user, pw) 
        end
        if not @user.nil?
          session[:user] = @user.name
          session[:logon]=true
        end
        
      else # Ansonsten zurückschicken zum Login
        flash['login']="Nicht eingelogt"
        redirect_to  :controller=>"user"
      end
    end
  end  
  
  # Erkennen des Betriebssystems
  def os
    if RUBY_PLATFORM =~ /linux/
      return "linux"
    elsif RUBY_PLATFORM =~/win/
      return "win"
    end
  end
  
  # admin garantieren
  def admin 
    if User.find_by_name(session[:user]).access != "admin"
      redirect_to  :controller=>"user"
    end
  end
  
  # Findet herraus ob der Benutzer die entsprechende Rolle hat
  # Da der Session-store in der SQL-Datenbank ist, besteht keine Gefahr
  def hasRole role
    roles="leecher","listener","tagger","admin"
    index = roles.index(role)
    if index != nil
      if User.find_by_name(session[:user]) != nil
        userindex= roles.index(User.find_by_name(session[:user]).access)
        if userindex >= index
          return true
        end
      end
      return false
    end
  end
end
