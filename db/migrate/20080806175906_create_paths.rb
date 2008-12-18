class CreatePaths < ActiveRecord::Migration
  def self.up
    create_table :paths do |t|
      t.text  :name
      t.text  :uri
    end
  end

  def self.down
    drop_table :paths
  end
end
