require 'validates_uri_existence_of'
class Server < ActiveRecord::Base
  belongs_to :user
  validates_presence_of :user_id
  validates_uri_existence_of :address, :with =>/^(http|https):\/\/((localhost)|#{DOMAIN}|#{NUMERIC_IP})#{PORT}$/
  
  def online?
    begin # check header response
      case Net::HTTP.get_response(URI.parse(self.address))
        when Net::HTTPSuccess then true
      else true
      end
    rescue # Recover on DNS failures..
      return false
    end
  end
  def available?
    begin # check header response
      case Net::HTTP.get_response(URI.parse(self.address))
        when Net::HTTPSuccess then true
      else false
      end
    rescue # Recover on DNS failures..
      return false
    end
  end
end
