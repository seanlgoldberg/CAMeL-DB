function [truth, quality, turkerAnswers, marg] = convertAMT(csvfile)

QUES_PER_HIT = 10;
NUM_TURKERS = 3;
turkerAnswers = zeros(1,NUM_TURKERS);
marg = cell(1,NUM_TURKERS);
quality = zeros(1,NUM_TURKERS);
truth = [];
margBlock = cell(QUES_PER_HIT*NUM_TURKERS,1);

fid = fopen(csvfile);

%Convert to cell array
%data = textscan(fid,'%q%q%q%q%q%q%q%q%q%q%q%q%q%q','delimiter',',');
data = textscan(fid,'%q%q%q%q%q%q%q%q%q%q%q%q%q%q%q%q%q%q','delimiter',',');

%Pull Answers & Truth
%HITs = unique(data{3});
HITs = unique(data{1});
for i=1:numel(HITs),
    for j=1:numel(data{3})
        %if strcmp(HITs{i},data{3}(j)),
        if strcmp(HITs{i},data{1}(j)),
%             block = data{5}(j:(j+(QUES_PER_HIT*NUM_TURKERS-1)));
%             qualityBlock = DawidSkene(block,QUES_PER_HIT,NUM_TURKERS);
%             truthBlock = data{6}(j:(j+(QUES_PER_HIT*NUM_TURKERS-1)));
            idx = j-1;
%              block = [str2double(data{9}(idx+1)), str2double(data{9}(idx+2)), str2double(data{9}(idx+3)), str2double(data{9}(idx+4)), str2double(data{9}(idx+5));
%                      str2double(data{10}(idx+1)), str2double(data{10}(idx+2)), str2double(data{10}(idx+3)), str2double(data{10}(idx+4)), str2double(data{10}(idx+5));
%                      str2double(data{11}(idx+1)), str2double(data{11}(idx+2)), str2double(data{11}(idx+3)), str2double(data{11}(idx+4)), str2double(data{11}(idx+5));
%                      str2double(data{12}(idx+1)), str2double(data{12}(idx+2)), str2double(data{12}(idx+3)), str2double(data{12}(idx+4)), str2double(data{12}(idx+5));
%                      str2double(data{13}(idx+1)), str2double(data{13}(idx+2)), str2double(data{13}(idx+3)), str2double(data{13}(idx+4)), str2double(data{13}(idx+5));
%                      str2double(data{14}(idx+1)), str2double(data{14}(idx+2)), str2double(data{14}(idx+3)), str2double(data{14}(idx+4)), str2double(data{14}(idx+5));
%                      str2double(data{15}(idx+1)), str2double(data{15}(idx+2)), str2double(data{15}(idx+3)), str2double(data{15}(idx+4)), str2double(data{15}(idx+5));
%                      str2double(data{16}(idx+1)), str2double(data{16}(idx+2)), str2double(data{16}(idx+3)), str2double(data{16}(idx+4)), str2double(data{16}(idx+5));
%                      str2double(data{17}(idx+1)), str2double(data{17}(idx+2)), str2double(data{17}(idx+3)), str2double(data{17}(idx+4)), str2double(data{17}(idx+5));
%                      str2double(data{18}(idx+1)), str2double(data{18}(idx+2)), str2double(data{18}(idx+3)), str2double(data{18}(idx+4)), str2double(data{18}(idx+5))];
            block = [str2double(data{9}(idx+1)), str2double(data{9}(idx+2)), str2double(data{9}(idx+3));
                     str2double(data{10}(idx+1)), str2double(data{10}(idx+2)), str2double(data{10}(idx+3)); 
                     str2double(data{11}(idx+1)), str2double(data{11}(idx+2)), str2double(data{11}(idx+3));
                     str2double(data{12}(idx+1)), str2double(data{12}(idx+2)), str2double(data{12}(idx+3)); 
                     str2double(data{13}(idx+1)), str2double(data{13}(idx+2)), str2double(data{13}(idx+3));
                     str2double(data{14}(idx+1)), str2double(data{14}(idx+2)), str2double(data{14}(idx+3)); 
                     str2double(data{15}(idx+1)), str2double(data{15}(idx+2)), str2double(data{15}(idx+3)); 
                     str2double(data{16}(idx+1)), str2double(data{16}(idx+2)), str2double(data{16}(idx+3)); 
                     str2double(data{17}(idx+1)), str2double(data{17}(idx+2)), str2double(data{17}(idx+3));
                     str2double(data{18}(idx+1)), str2double(data{18}(idx+2)), str2double(data{18}(idx+3))];

             qualityBlock = DawidSkene(block,QUES_PER_HIT,NUM_TURKERS,1);
             truthBlock = [str2double(data{9}(idx+6));
                           str2double(data{10}(idx+6));
                           str2double(data{11}(idx+6));
                           str2double(data{12}(idx+6));
                           str2double(data{13}(idx+6));
                           str2double(data{14}(idx+6));
                           str2double(data{15}(idx+6));
                           str2double(data{16}(idx+6));
                           str2double(data{17}(idx+6));
                           str2double(data{18}(idx+6))];
             
             
             turkerAnswers = [turkerAnswers; block];
             quality = [quality; repmat(qualityBlock,[QUES_PER_HIT 1])];
             truth = [truth; truthBlock];
%             margCount = 1;
%             %marginal blocks
%             for k=j:(j+(QUES_PER_HIT*NUM_TURKERS-1))
%                 margBlock{margCount} = [str2double(data{7}(k)), str2double(data{8}(k)),...
%                                str2double(data{9}(k)), str2double(data{10}(k)),...
%                                str2double(data{11}(k)), str2double(data{12}(k)),...
%                                str2double(data{13}(k)), str2double(data{14}(k))];
%                 margCount = margCount + 1;
%             end
            
%             turkerAnswers = [turkerAnswers; zeros(QUES_PER_HIT,NUM_TURKERS)];
%             marg = [marg; cell(QUES_PER_HIT,NUM_TURKERS)];
%             quality = [quality; repmat(qualityBlock,[QUES_PER_HIT 1])];
%             count = 1;
%             for i3=1:NUM_TURKERS,
%                 for i2=1:QUES_PER_HIT,
%                     turkerAnswers(((i-1)*QUES_PER_HIT+i2),i3) = str2double(block{count});
%                     marg{(i-1)*QUES_PER_HIT+i2,i3} = margBlock{count};
%                     %Only pull truth once
%                     if (i3==1)
%                         truth(((i-1)*QUES_PER_HIT+i2),1) = str2double(truthBlock{count});
%                     end
%                     count = count + 1;
%                 end
%             end
            break;
        end
    end
end

%[turkerAnswers,~] = removerows(turkerAnswers,size(turkerAnswers,1));
[turkerAnswers,~] = removerows(turkerAnswers,1);
marg = marg(1:(end-1),1);
[quality,~] = removerows(quality,1);

