class CreateGenres < ActiveRecord::Migration
  def self.up
    create_table :genres do |t|
      t.text  :name
      t.integer :path_id
    end
  end

  def self.down
    drop_table :genres
  end
end
