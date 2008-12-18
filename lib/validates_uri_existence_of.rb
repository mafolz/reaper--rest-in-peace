require 'net/http'
# Komplettes Copy & Paste von http://www.igvita.com/2006/09/07/validating-url-in-ruby-on-rails/
# Original credits: http://blog.inquirylabs.com/2006/04/13/simple-uri-validation/
# HTTP Codes: http://www.ruby-doc.org/stdlib/libdoc/net/http/rdoc/classes/Net/HTTPResponse.html
PORT = /(([:]\d+)?)/
DOMAIN = /([a-z0-9\-]+\.?)*([a-z0-9]{2,})\.[a-z]{2,}/
NUMERIC_IP = /(?>(?:1?\d?\d|2[0-4]\d|25[0-5])\.){3}(?:1?\d?\d|2[0-4]\d|25[0-5])(?:\/(?:[12]?\d|3[012])|-(?>(?:1?\d?\d|2[0-4]\d|25[0-5])\.){3}(?:1?\d?\d|2[0-4]\d|25[0-5]))?/

class ActiveRecord::Base
  def self.validates_uri_existence_of(*attr_names)
    configuration = { :message => "is not valid or not responding", :on => :save, :with => nil }
    configuration.update(attr_names.pop) if attr_names.last.is_a?(Hash)
 
    raise(ArgumentError, "A regular expression must be supplied as the :with option of the configuration hash") unless configuration[:with].is_a?(Regexp)
 
    validates_each(attr_names, configuration) do |r, a, v|
        if v.to_s =~ configuration[:with] # check RegExp
              begin # check header response
                  case Net::HTTP.get_response(URI.parse(v))
                    when Net::HTTPSuccess then true
                    else r.errors.add(a, configuration[:message]) and false
                  end
              rescue # Recover on DNS failures..
                  r.errors.add(a, configuration[:message]) and false
              end
        else
          r.errors.add(a, configuration[:message]) and false
        end
    end
  end
end
 