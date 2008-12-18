class RemoteSong < ActiveRecord::Base
  include RemoteHelper
  self.element_name="Song"
end
