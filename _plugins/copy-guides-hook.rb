Jekyll::Hooks.register :site, :post_write do |site|
  puts 'Copying guide content'
  `_plugins/copy-guides.sh`
end
