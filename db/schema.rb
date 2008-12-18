# This file is auto-generated from the current state of the database. Instead of editing this file, 
# please use the migrations feature of Active Record to incrementally modify your database, and
# then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your database schema. If you need
# to create the application database on another system, you should be using db:schema:load, not running
# all the migrations from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended to check this file into your version control system.

ActiveRecord::Schema.define(:version => 20080902060227) do

  create_table "artists", :force => true do |t|
    t.text     "name"
    t.integer  "genre_id"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "genres", :force => true do |t|
    t.text    "name"
    t.integer "path_id"
  end

  create_table "paths", :force => true do |t|
    t.text "name"
    t.text "uri"
  end

  create_table "radiostations", :force => true do |t|
    t.text     "address"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "servers", :force => true do |t|
    t.text     "address"
    t.integer  "user_id"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "sessions", :force => true do |t|
    t.string   "session_id", :null => false
    t.text     "data"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "sessions", ["session_id"], :name => "index_sessions_on_session_id"
  add_index "sessions", ["updated_at"], :name => "index_sessions_on_updated_at"

  create_table "songs", :force => true do |t|
    t.text    "title"
    t.text    "format"
    t.text    "location"
    t.integer "artist_id"
    t.boolean "localFlag",  :default => false
    t.date    "created_at"
  end

  create_table "streamrippers", :force => true do |t|
    t.text     "other"
    t.integer  "radiostation_id"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "users", :force => true do |t|
    t.text     "name"
    t.text     "password"
    t.text     "access"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

end
