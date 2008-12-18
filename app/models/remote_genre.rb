class RemoteGenre < ActiveResource::Base
  include RemoteHelper
  self.element_name="Genre"  
 
end
