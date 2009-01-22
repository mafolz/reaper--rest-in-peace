# To change this template, choose Tools | Templates
# and open the template in the editor.
require 'lastfm_search'
class GetID3
  def initialize
      @lastfm = LastfmSearch.new
  end

  def get_tags( artist, track )
    @lastfm.search( artist, track )
  end

  def get_genre( artist )
    @lastfm.get_genre( artist)
  end
end