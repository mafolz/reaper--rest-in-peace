# To change this template, choose Tools | Templates
# and open the template in the editor.

require 'yahoo_cover_search'

describe YahooCoverSearch do
  before(:each) do
    @yahoo_cover_search = YahooCoverSearch.new
  end

  it "should desc" do
    puts @yahoo_cover_search.search("corvus corax", "viator")
  end
end

