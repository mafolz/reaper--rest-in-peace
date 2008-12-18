module SettingsHelper
  def subapps
    apps= [{"name"=>"Streamripper","cmd"=>"streamripper","required"=>true},
    {"name"=>"Last.FM Proxy","cmd"=>"lastproxy","required"=>false}]    
    wert=""
    apps.each do |app|
      wert += '<div class="song-pane" id="statusrow" >'
      wert += "<div id=\"statusblock\">#{app["name"]}</div>"
      if app["required"]
        wert += '<div id="statusblock">[ben&ouml;tigt]</div>'
      else
        wert += '<div id="statusblock">[optional]</div>'
      end
      if os=="linux"
        require "ftools"
        if File.exist?("/usr/bin/#{app["cmd"]}")
          wert += '<div id="tag-status">Ok</div>'
        else
          wert += '<div id="tag-status-error">nicht instaliert</div>'          
        end
      end      
      wert +='</div>'
    end
    return wert
  end
end
