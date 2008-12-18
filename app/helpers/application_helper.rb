# Methods added to this helper will be available to all templates in the application.
module ApplicationHelper    
  def version
    return "0.2-Pre"
  end
  # Erkennen des Betriebssystems
  def os
    if RUBY_PLATFORM =~ /linux/
      return "linux"
    elsif RUBY_PLATFORM =~/win/
      return "win"
    end
  end
  
  # Tooltip helper
  # dem Aufruf wird der Inhalt des Tooltips übergeben
  # Dieser kann auch HTML beinhalten
  def tooltip text
    return "
    onmouseout=\"toolie.hide()\"
    onmouseover=\"toolie.showWith(\'#{text.gsub(/\n/,'')}\')\""
  end
  
  
  # Findet herraus ob der Benutzer die entsprechende Rolle hat
  # Da der Session-store in der SQL-Datenbank ist, besteht keine Gefahr
  def hasRole role  # Array der Rollen, um eine Hirachie aufzubauen
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
  # Leitet vom ActionView zum Controller para weiter
  def redirect_to para
    return'<script type="text/javascript">window.document.location.href="redirector/goto/'+para+'";</script>'
  end
  
end
