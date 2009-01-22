# To change this template, choose Tools | Templates
# and open the template in the editor.
require 'uri'
require "digest"

class LastfmSearch
  
  # Your API Key is 14f8f6cc8c14ff043dc021d89f7ebdf4
  def initialize
    @API_KEY = "14f8f6cc8c14ff043dc021d89f7ebdf4"
    
    # track informationsurl
    @TRACKURL = "http://ws.audioscrobbler.com/2.0/?method=track.getinfo&api_key=#{@API_KEY}&format=json"
    # Artisteninformations URL
    @ARTISTURL = "http://ws.audioscrobbler.com/2.0/?method=artist.gettoptags&api_key=#{@API_KEY}&format=json"
    
  end
  
  # Eine Artisteninformationsabfrage
  def get_genre(artist)
    @artist = artist
    
    get_data
    
    # Wenn fehler in der ABfrage oder Nichts gefunden wurde
    if @objectResult.nil? or @objectResult["toptags"].nil?
      return "unknown"
    end
    
    unless @objectResult["toptags"]["tag"].nil?
      return @objectResult["toptags"]["tag"].first["name"]
    end
    # bestes Ergebnis ausfiltern
    return "unknown"
  end
  
  def search(artist, track)
    @artist = artist#.sub(/\W/, /%20/)
    @track = track#.sub(/\W/, /%20/)
    get_data
    
    result = {}
    
    # Wenn keine Albuminformation vorhanden ist
    if @objectResult.nil? or 
      @objectResult["track"].nil? or 
      @objectResult["track"]["album"].nil?
      result[:album] = "unknown"
      return result
    end
    result[:album] = @objectResult["track"]["album"]["title"]
    result[:track] = @objectResult["track"]["album"]["position"]
    
    unless @objectResult["track"].empty? or 
      @objectResult["track"]["toptags"].empty? or  
      @objectResult["track"]["toptags"]["tag"].empty? 
      # tags could be a Array of Tags or only one Element
      # These matter must be handled
      tags =  @objectResult["track"]["toptags"]["tag"]
      if tags.class == Array 
        result[:genre] = tags.first["name"] 
      else
        result[:genre] = tags
      end
    end
    
    image_array = @objectResult["track"]["album"]["image"]
    
    image_array.each do |image|
      if image["size"] == "large"
        result[:cover] = image["#text"]
      end
    end
    if result[:cover].nil?
      image_array.each do |image|
        if image["size"] == "medium"
          result[:cover] = image["#text"]
        end
      end
    end
    # bestes Ergebnis ausfiltern
    return result
  end
  
  def get_data
    
    puts "\n\nURL " + build_url + "\n\n"
    response  = Net::HTTP.get_response(URI.parse(  build_url ))
    case response
      when Net::HTTPOK  # => Wenn HTTP-Response in Ordnung
      begin
        @objectResult = ActiveSupport::JSON.decode(response.body)
        # Hieraus kann die JSON-Klasse ein Ruby-Objekt erzeugen
      rescue
        raise @URL +  " sends a invalid JSON-Stream"
      end
    else
      puts  @URL  + " says " + response.inspect
      return nil
    end
    
    unless @objectResult["message"].nil?
      return nil
    end
  end
  
  def build_url
    # Wenn kein track gesetzt wurde, suche über Artisten Informationen
    if @track.nil? 
      # Für diese Information muss sich bei LastFM authentifiziert werden
      return URI.escape(@ARTISTURL + "&artist=" + @artist)
    end
    return URI.escape(@TRACKURL + "&artist=" + @artist + "&track=" + @track)
    
  end
  
  def bulk
    return  {"track"=>
      {"artist"=>   {"name"=>"Air", "url"=>"http://www.last.fm/music/Air", "mbid"=>"cb67438a-7f50-4f2b-a6f1-2bb2729fd538"},
        "duration"=>"278000", "name"=>"Alpha Beta Gaga",
        "url"=>"http://www.last.fm/music/Air/_/Alpha+Beta+Gaga",
        "toptags"=>{"tag"=>[{"name"=>"electronic", "url"=>"http://www.last.fm/tag/electronic"},
          {"name"=>"chillout", "url"=>"http://www.last.fm/tag/chillout"},
          {"name"=>"electronica", "url"=>"http://www.last.fm/tag/electronica"},
          {"name"=>"ambient", "url"=>"http://www.last.fm/tag/ambient"},
          {"name"=>"french", "url"=>"http://www.last.fm/tag/french"}]},
        "mbid"=>"",
        "id"=>"2798716",
        "album"=>{"position"=>"8", "artist"=>"Air", "title"=>"Talkie Walkie",
          "url"=>"http://www.last.fm/music/Air/Talkie+Walkie", "mbid"=>"b7b7e4fe-2e1d-48bd-bca9-67fd61f43df6",
          "image"=>[{"size"=>"small", "#text"=>"http://userserve-ak.last.fm/serve/64s/15191047.jpg"},
          {"size"=>"medium", "#text"=>"http://userserve-ak.last.fm/serve/126/15191047.jpg"},
          {"size"=>"large", "#text"=>"http://userserve-ak.last.fm/serve/174s/15191047.jpg"}]},
        "streamable"=>{"fulltrack"=>"1", "#text"=>"1"}, "playcount"=>"676834", "listeners"=>"154395"}}
    
  end
end
