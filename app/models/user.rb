class User < ActiveRecord::Base  
  # Schön ^^ der uniques-Helper streitet sich mit PostgreSQL
  # Wegen des fehlens von Anführunszeichen in den entsprehcenden Rails-Adaptern (Bug)
  # SELECT "Name" FROM "users"     WHERE ("users".Name = E'test' AND "users".Name = E'test') 
  # wobei es "users"."Name" heißen muss!
  validates_uniqueness_of :name, :scope =>:name  
  validates_presence_of :name, :password
  validates_format_of :access, :with => /\A(listener)|(leecher)|(tagger)|(admin)\Z/
  
  def validate_on_create
    # Leerzweichen entfernen
    self.name=self.name.strip
    self.password=self.password.strip
    
    if User.exists?(:name=> self.name)
      errors.add(:User,"schon vorhanden")
    end
  end
  def changeAccess mod  
    self.Access=mod    
    self.updated_at=Time.now
  end
end
