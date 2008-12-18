# To change this template, choose Tools | Templates
# and open the template in the editor.

class Special::YahooCoverSearch
  @URL = "http://boss.yahooapis.com/ysearch/images/v1/"
  @QUERYTAIL = "?appid=T.R4sfbV34EnMTQpc1bOwFkNfKSU.kIOBBuYqnNMsDH2MqcPw00Ed7dn6sd.fce6jA6RKA--&format=json&filter=yes"
  # Beispielurl
  # http://boss.yahooapis.com/ysearch/images/v1/corvus%20corax%20viator?appid=T.R4sfbV34EnMTQpc1bOwFkNfKSU.kIOBBuYqnNMsDH2MqcPw00Ed7dn6sd.fce6jA6RKA--&format=json&filter=yes
  @RANKING = ["www.amazon.de",
              "www.amazon.fr",
              "picamusic.de",
              "www.progmusic.ru",
              "musicmp3.ru",
              "www.outersound.com",
              "themp3down.info"]

  def search(artist, album)
    @artist = artist.sub(/\W/, /%20/)
    @album = album.sub(/\W/, /%20/)
    return getData
  end
  def get_data
    response  = Net::HTTP.get_response(URI.parse(  build_url ))
    case response
    when Net::HTTPOK  # => Wenn HTTP-Response in Ordnung
      begin
        @objectResult = ActiveSupport::JSON.decode(response.body)["ysearchresponse"]["resultset_images"]
        # Hieraus kann die JSON-Klasse ein Ruby-Objekt erzeugen
      rescue
        raise @URL +  " sends a invalid JSON-Stream"
      end
    else
      puts  @URL  + " says " + response.inspect
      return ""
    end

    # bestes Ergebnis ausfiltern
    # clickurl oder url => Bildadresse , height => Bildgröße, size => Dateigröße
    # Dran denken, URLs sind Escaped: "refererurl":"http:\/\/themp3down.info\/viator\/alb11913"

    end

  def build_url
      return @URL + @artist + "%20" + @album + @QUERYTAIL
  end
end
