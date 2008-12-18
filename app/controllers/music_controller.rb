class MusicController < ApplicationController
  before_filter :logon
  
  def remoteGenre
     render :layout => "none"
  end
  def remoteArtist
     render :layout => "noe"
     @server=Server.find(params[:server_id])
     RemoteArtist.setServer(@server)
     #  Hier leigt der HUnd begraben, aber wieso?
     @remoteArtists=RemoteArtist.find(:all, :params=>{:Genre_id=>params[:id].to_i})
  end
end
