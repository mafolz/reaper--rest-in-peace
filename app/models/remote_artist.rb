class RemoteArtist < ActiveResource::Base
  include RemoteHelper
  self.element_name="Artist"
  
end
