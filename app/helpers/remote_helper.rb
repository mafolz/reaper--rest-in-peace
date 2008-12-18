# Trick aus http://snippets.dzone.com/posts/show/348
module RemoteHelper
  def self.append_features(base) # :nodoc:
    super
    base.extend ClassMethods
  end
  
  module ClassMethods
    # Server setzen
    def setServer server
      self.site=server.address
      self.user=server.user.name
      self.password=server.user.password
    end
  end
  
end
