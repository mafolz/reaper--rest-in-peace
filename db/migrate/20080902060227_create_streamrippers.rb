class CreateStreamrippers < ActiveRecord::Migration
  def self.up
    create_table :streamrippers do |t|
      t.text :other
      t.integer :radiostation_id 

      t.timestamps
    end
  end

  def self.down
    drop_table :streamrippers
  end
end
